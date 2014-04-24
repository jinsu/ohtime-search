package com.ojk.ohtime.search.services

import akka.actor.Actor
import spray.http.MediaTypes._
import spray.httpx.marshalling.ToResponseMarshallable.isMarshallable
import spray.routing.Directive.pimpApply
import akka.actor.ActorRef
import com.ojk.ohtime.search.domain._
import com.ojk.ohtime.search.engine.SearchEngine._
import com.ojk.ohtime.search.engine.SearchEngine

// we don't implement our route structure directly in the service actor because
// we want to be able to test it independently, without having to spin up an actor
class SearchService(searchEngine: ActorRef) extends Actor {
  var results = Option.empty[RestMessage]

  def receive = {
    case SearchRequest(q) => {
      searchEngine ! EngineQuery(q)
      context.become(waitingResponses)
    }
  }

  def waitingResponses: Receive = {
    case EngineResultSet(msg) => {
      results = Some(SearchResponse(msg))
      replyIfReady
    }
    case Error(msg) => {
      println("Received error: " + msg);
    }
  }

  def replyIfReady =
    if (results.nonEmpty) {
      // some processing logic
      context.parent ! results.get
    }
}
