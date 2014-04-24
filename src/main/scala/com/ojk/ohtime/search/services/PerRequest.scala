package com.ojk.ohtime.search.services

import com.ojk.ohtime.search.domain.RestMessage
import spray.http.StatusCode
import spray.httpx.Json4sSupport
import akka.actor.ReceiveTimeout
import spray.routing.RequestContext
import scala.concurrent.duration._
import akka.actor._
import spray.http.StatusCode
import spray.http.StatusCodes._
import org.json4s.DefaultFormats
import com.ojk.ohtime.search.services.PerRequest._
import com.ojk.ohtime.search.domain._

trait PerRequest extends Actor with Json4sSupport {
  import context._

  val json4sFormats = DefaultFormats

  def r: RequestContext
  def target: ActorRef
  def msg: RestMessage

  setReceiveTimeout(2.seconds)
  target ! msg

  def receive = {
    case res: RestMessage => complete(OK, res)
    case v: Validation => complete(BadRequest, v)
    case ReceiveTimeout => complete(GatewayTimeout, Error("Request Timeout"))
  }

  def complete[T <: AnyRef](status: StatusCode, obj: T) {
    r.complete(status, obj)
    stop(self)
  }

}

object PerRequest {
  case class WithActorRef(r: RequestContext, target: ActorRef, msg: RestMessage) extends PerRequest

  case class WithProps(r: RequestContext, props: Props, msg: RestMessage) extends PerRequest {
    lazy val target = context.actorOf(props)
  }
}

trait PerRequestCreator { this: Actor =>

  def perRequest(r: RequestContext, target: ActorRef, msg: RestMessage) =
    context.actorOf(Props(new WithActorRef(r, target, msg)))

  def perRequest(r: RequestContext, props: Props, msg: RestMessage) =
    context.actorOf(Props(new WithProps(r, props, msg)))
}
