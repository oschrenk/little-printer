package lp.web

import java.io.ByteArrayInputStream

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.Sink
import akka.stream.{ActorAttributes, Supervision}
import javax.imageio.ImageIO
import lp.image.Transformations._
import lp.model.{Bridge, Printer}

import scala.concurrent.ExecutionContextExecutor
import scala.util.Try

object Routes {

  def build(serverActor: ActorRef, decider: Supervision.Decider)(implicit
      system: ActorSystem,
      executionContext: ExecutionContextExecutor
  ): Route =
    path("socket") {
      handleWebSocketMessages(
        WebUserFlow.buildWith(serverActor)
      )
    } ~ path("upload") {
      fileUpload("image") {
        case (_, byteSource) =>
          // would use field unmarshalling here, but since both refined types
          // use the same ruleset, akka unmarshaller can't disambiguate
          formFields(Symbol("bridge"), Symbol("printer")) {
            case (bridge, printer) =>
              (for {
                bridge <- Bridge.Address(bridge)
                printer <- Printer.Address(printer)
              } yield (bridge, printer)) match {
                case Left(s) =>
                  complete(StatusCodes.BadRequest, s)
                case Right((bridge, printer)) =>
                  complete {
                    byteSource
                      .runWith(Sink.fold(Array.empty[Byte]) {
                        case (acc, bs) => acc ++ bs.toList
                      })
                      .map { bytes =>
                        Try(ImageIO.read(new ByteArrayInputStream(bytes))).foreach { image =>
                          serverActor ! ServerActor.Print(
                            bridge,
                            printer,
                            resizeToWidth(image, 384)
                          )
                        }
                      }
                      .map(_ => StatusCodes.OK)
                  }
              }
          }
      }
    } ~ path("api" / "v1" / "connection") {
      handleWebSocketMessages(
        BridgeFlow
          .buildWith(system.actorOf(BridgeActor.props(serverActor)))
          .withAttributes(ActorAttributes.supervisionStrategy(decider))
      )
    }
}
