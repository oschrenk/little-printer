package lp.web

import akka.NotUsed
import akka.actor.ActorRef
import akka.http.scaladsl.model.ws.TextMessage.Streamed
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{Materializer, OverflowStrategy}
import akka.util.Timeout
import lp.hardware.protocol.in.Incoming
import lp.hardware.protocol.out.Outgoing

import scala.concurrent.duration._

object BridgeFlow {
  implicit private val timeout: Timeout = 3.seconds
  private val DefaultBufferSize = 10

  def buildWith(
      bridgeActor: ActorRef
  )(implicit
      m: Materializer
  ): Flow[Message, TextMessage.Strict, NotUsed] = {

    val incomingMessages: Sink[Message, NotUsed] = {
      Flow[Message]
        .map {
          case TextMessage.Strict(text) =>
            Incoming.parse(text) match {
              case Left(e) =>
                throw new IllegalArgumentException(s"$e via `$text`")
              case Right(protocol) => protocol
            }
          // ignore other messages but drain content to avoid stream being clogged
          case bm: BinaryMessage => bm.dataStream.runWith(Sink.ignore)
          case Streamed(t)       => t.runWith(Sink.ignore)
        }
        .collect { case p: Incoming => p }
        .to(
          Sink.actorRef(
            bridgeActor,
            BridgeActor.Disconnected,
            _ => BridgeActor.Disconnected
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
        bridgeActor ! BridgeActor.Connected(outActor)
        akka.NotUsed
      }
      .map((outgoing: Outgoing) =>
        TextMessage.Strict(Outgoing.encode(outgoing).toString())
      )
    
    Flow.fromSinkAndSourceCoupled(incomingMessages, outgoingMessages)
  }

}
