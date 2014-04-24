package com.ojk.ohtime.search.engine

import akka.actor.Actor
import com.ojk.ohtime.search.engine.SearchEngine._
import org.apache.solr.client.solrj.impl.HttpSolrServer
import java.net.URL
import com.ojk.ohtime.search.domain._
import org.apache.solr.client.solrj.SolrServer
import org.apache.solr.client.solrj.response.UpdateResponse
import org.apache.solr.client.solrj.request.QueryRequest
import org.apache.solr.common.SolrDocument
import org.apache.solr.client.solrj.response.QueryResponse
import org.apache.solr.client.solrj.SolrQuery
import org.apache.solr.common.SolrInputDocument
import scala.concurrent.Promise
import scala.concurrent.Future
import scala.util.Failure
import scala.util.Success
import concurrent.ExecutionContext.Implicits.global

class SearchEngine extends Actor {

  val solr: SolrServer = new HttpSolrServer("http://localhost:8080/soju/ohtime");
  val commitWithinMS: Int = 120000

  def receive = {
    case EngineQuery(q) => {
      println("query received")
      sender ! EngineResultSet(getFirst(search(createQuery(q))))
    }
    case EngineIndexUpdate(msg: Map[String, Any]) => {
      println("index update received")
      val doc = new SolrInputDocument
      msg.foreach {
        case (k, v) =>
          doc.addField(k, v)
      }

      val response: UpdateResponse = solr.add(doc, commitWithinMS)
      solr.commit()
      if (response.getStatus == 0) {
        println("response arrived")
        sender ! EventStatus(Option(true))
      } else {
        sender ! Error("Error while processing index update: solr error code: " + response.getStatus)
      }
    }
    case EngineBulkUpdate(futureMsgs) => {
      println("Bulk Update triggered")
      futureMsgs onComplete {
        case Success(msgs: List[Map[String, Any]]) => {

          val response: UpdateResponse = solr.add(createSolrDocList(msgs), commitWithinMS)
          solr.commit()
          if (response.getStatus == 0) {
            println("response arrived")
            sender ! EventStatus(Option(true))
          } else {
            sender ! Error("Error while processing index update: solr error code: " + response.getStatus)
          }
        }
        case Failure(er) => sender ! Error(er.toString())
      }

    }
  }

  def createSolrDoc(msg: Map[String, Any]): SolrInputDocument = {
    val doc = new SolrInputDocument
    msg.foreach {
      case (k, v) =>
        doc.addField(k, v)
    }
    doc
  }

  def createSolrDocList(msgs: List[Map[String, Any]]): java.util.List[SolrInputDocument] = {
    val docs = new java.util.ArrayList[SolrInputDocument]
    msgs.foreach(msg => docs.add(createSolrDoc(msg)))
    docs
  }

  def createQuery(q: String): SolrQuery = {
    var parameters: SolrQuery = new SolrQuery()
    parameters.set("q", q)
    parameters
  }

  def search(req: SolrQuery): QueryResponse = {
    val response: QueryResponse = solr.query(req)
    response
  }

  def getFirst(resp: QueryResponse): String = {
    resp.getResults.get(0).getFieldValue("name").toString()
  }
}

object SearchEngine {
  case class EngineQuery(msg: String)
  case class EngineResultSet(msg: String)
  case class EngineIndexUpdate(doc: Map[String, Any])
  case class EngineBulkUpdate(docs: Future[List[Map[String, Any]]])
}
