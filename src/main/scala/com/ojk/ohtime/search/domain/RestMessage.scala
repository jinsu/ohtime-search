package com.ojk.ohtime.search.domain

trait RestMessage

case class SearchRequest(q: String) extends RestMessage
case class SearchResponse(q: String) extends RestMessage
case class IndexUpdateRequest(msg: Map[String, Any]) extends RestMessage
case class BulkUpdateRequest(source: String) extends RestMessage
case class EventStatus(outcome: Option[Boolean] = Option(false)) extends RestMessage

case class Validation(msg: String)
case class Error(msg: String)
