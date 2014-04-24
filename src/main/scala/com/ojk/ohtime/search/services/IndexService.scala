package com.ojk.ohtime.search.services

import akka.actor.Actor
import akka.actor.ActorRef
import com.ojk.ohtime.search.domain._
import com.ojk.ohtime.search.engine.SearchEngine._
import akka.actor.ActorSystem
import scala.concurrent.Future
import spray.http.HttpRequest
import spray.http.HttpResponse
import spray.client.pipelining._
import org.json4s.JsonAST.JObject
import org.json4s.Formats
import org.json4s.DefaultFormats
import spray.httpx._
import scala.util.Success
import scala.util.Failure
import org.json4s.JsonAST.JArray
import scala.concurrent.promise
import scala.concurrent.Promise
import java.io.IOException

class IndexService(searchEngine: ActorRef)(implicit system: ActorSystem) extends Actor with Json4sSupport {
  import system.dispatcher
  implicit val json4sFormats: Formats = DefaultFormats

  val pipeline: HttpRequest => Future[JArray] = (
    sendReceive
    ~> unmarshal[JArray])

  var results = Option.empty[RestMessage]

  def receive = {
    case IndexUpdateRequest(msg) => {
      searchEngine ! EngineIndexUpdate(msg)
      context.become(waitingResponses)
    }
    case BulkUpdateRequest(source) => {
      searchEngine ! EngineBulkUpdate(fetchJsonData("http://localhost:9000/biz.json?p.limit=10&p.offset=0"))
      context.become(waitingResponses)
    }
  }

  def waitingResponses: Receive = {
    case EventStatus(status) => {
      if (!status.get) {
        println("Received error")
      } else {
        results = Some(EventStatus(status))
        println("Received success event")
        replyIfReady
      }
    }
  }

  def replyIfReady =
    if (results.nonEmpty) {
      context.parent ! results.get
    }

  def fetchJsonData(url: String): Future[List[Map[String, Any]]] = {
    val respFuture = pipeline(Get(url))
    var docs = Promise[List[Map[String, Any]]]
    respFuture onComplete {
      case Success(obj: JArray) => {
        val data = obj.values.asInstanceOf[List[Map[String, Any]]]
        val processed = data map (datum => {
          val prices = datum.get("drinks").getOrElse(Map[String, Any]()).asInstanceOf[Map[String, Any]] map {
            case (name, price) =>
              (name + "_price", price)
          }
          datum -("drinks") ++ prices
        })

        docs.success(processed)
        println(docs)
      }
      case Success(unexpected) => {
        docs.failure(new IOException("Unexpected response"))
        println("Unexpected respons")
      }
      case Failure(error) => {
        println(error.toString)
        docs.failure(error)
      }
    }
    return docs.future
  }

}
