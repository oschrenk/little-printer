package lp.web

import java.util.UUID

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.TextMessage.Streamed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import lp.web.socket.Incoming.ClaimPrinter
import lp.web.socket.Outgoing
import play.api.libs.json.{JsError, JsSuccess, Json}

import scala.concurrent.duration._

object WebUserFlow {
  implicit private val timeout: Timeout = 3.seconds
  private val DefaultBufferSize = 10

  def buildWith(
      serverActor: ActorRef
  )(implicit
      m: Materializer
  ): Flow[Message, TextMessage.Strict, NotUsed] = {

    // TODO how do I get access to the outActor from outgoing?
    // I would love to use that here as an implicit id
    // look into preMaterialise as an option for that
    val uuid = UUID.randomUUID()
    val incomingMessages: Sink[Message, NotUsed] = {
      Flow[Message]
        .map {
          case TextMessage.Strict(text) =>
            Json.fromJson[ClaimPrinter](Json.parse(text)) match {
              case JsSuccess(value, _) =>
                value
              case JsError(errors) =>
                // TODO what do on errors?
            }
          // ignore other messages but drain content to avoid stream being clogged
          case bm: BinaryMessage => bm.dataStream.runWith(Sink.ignore)
          case Streamed(t)       => t.runWith(Sink.ignore)
        }
        .collect { case cp: ClaimPrinter => cp }
        .to(
          Sink.actorRef(
            serverActor,
            ServerActor.Closed(uuid),
            _ => ServerActor.Closed(uuid)
          )
        )
    }

    val outgoingMessages = Source
      .actorRef(
        PartialFunction.empty, // never complete
        PartialFunction.empty, // never fail
        DefaultBufferSize,
        OverflowStrategy.fail
      )
      .mapMaterializedValue { outActor =>
        // give the server a way to send messages out
        serverActor ! ServerActor.Registered(uuid, outActor)
        akka.NotUsed
      }
      .map((outgoing: Outgoing) => TextMessage.Strict(Json.toJson(outgoing).toString()))

    Flow.fromSinkAndSourceCoupled(incomingMessages, outgoingMessages)
  }

}
