package com.ojk.ohtime.search.services

import spray.httpx._
import akka.actor.Actor
import spray.http.MediaTypes._
import spray.routing.HttpService
import com.ojk.ohtime.search.domain._
import akka.actor.Props
import spray.routing.Route
import com.ojk.ohtime.search.engine.SearchEngine
import org.json4s.DefaultFormats
import org.json4s.JsonAST.JObject
import org.json4s.Formats
import akka.actor.ActorSystem

class RestRouting(implicit system: ActorSystem) extends HttpService with Actor with PerRequestCreator with Json4sSupport {
  implicit val json4sFormats: Formats = DefaultFormats

  def actorRefFactory = context

  val searchEngine = context.actorOf(Props[SearchEngine])

  val viewRoutes = {
    path("") {
      get {
          complete {
            "Work work"
          }
      }
    }
  }

  val jsonRoutes = {
    path("search") {
      get {
        parameters('q.as[String]) { (q) =>
          processSearchRequest {
            SearchRequest(q)
          }
        }
      }
    }
  }

  val updateRoutes = {
    path("update") {
      post {
        decompressRequest() {
          entity(as[JObject]) { data =>
            val doc = data.values.asInstanceOf[Map[String, Any]]
            processIndexRequest {
              IndexUpdateRequest(doc)
            }
          }
        }
      }
    }
  }

  val indexRoutes = {
    path("index") {
      get {
        parameters('s.as[String]) { (s) =>
          processIndexRequest {
            BulkUpdateRequest(s)
          }
        }
      }
    }
  }

  val allRoutes = viewRoutes ~ jsonRoutes ~ updateRoutes ~ indexRoutes

  def receive = runRoute(allRoutes)

  def processSearchRequest(message: RestMessage): Route = {
    ctx => perRequest(ctx, Props(new SearchService(searchEngine)), message)
  }

  def processIndexRequest(message: RestMessage): Route = {
    ctx => perRequest(ctx, Props(new IndexService(searchEngine)), message)
  }

}

