package lp

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.Supervision
import lp.web.{Routes, ServerActor}

import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main {
  private val logger = com.typesafe.scalalogging.Logger("LittlePrinter")

  def main(args: Array[String]): Unit = {

    implicit val system: ActorSystem = ActorSystem()
    implicit val executionContext: ExecutionContextExecutor = system.dispatcher

    val config = Config.load.toOption.get

    val decider: Supervision.Decider = {
      case e: IllegalArgumentException =>
        logger.error("Supervisor caught error. Resuming.", e)
        Supervision.Resume
      case _ => Supervision.Stop
    }

    val serverActor = system.actorOf(ServerActor.props())
    val route = Routes.build(serverActor, decider)

    Http()
      .bindAndHandle(route, config.interface, config.port)
      .onComplete {
        case Success(value) =>
          logger.info(s"Server online at http://${value.localAddress}")
        case Failure(exception) =>
          logger.error(exception.getMessage)
      }
  }
}
