package lp.web

import java.awt.image.BufferedImage
import java.util.UUID

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import lp.model.{Bridge, Printer}
import lp.web

object ServerActor {
  def props(): Props = Props(new ServerActor())

  // manage actors for bridge-facing websocket connections
  case class Connected(id: Bridge.Address, bridgeActor: ActorRef)
  case class Disconnected(id: Bridge.Address)

  // manage actors for user-facing websocket connections
  case class Registered(id: UUID, outActor: ActorRef)
  case class Closed(id: UUID)

  // manage commands from user
  case class Print(
      bridge: Bridge.Address,
      printer: Printer.Address,
      image: BufferedImage
  )
}
class ServerActor extends Actor with ActorLogging {
  override def receive: Receive = connected(Map.empty, Map.empty)

  def connected(
      bridges: Map[Bridge.Address, ActorRef],
      webClients: Map[UUID, ActorRef]
  ): Receive = {
    // manage actors for bridge-facing websocket connections
    case ServerActor.Connected(id, bridgeActor) =>
      log.info("Connecting bridge {}", id)
      context.become(connected(bridges + (id -> bridgeActor), webClients))
    case ServerActor.Disconnected(id) =>
      log.info("Disconnecting bridge {}", id)
      val newBridges = bridges.filter { case (k, _) => k != id }
      context.become(connected(newBridges, webClients))

    // manage actors for user-facing websocket connections
    case ServerActor.Registered(id, outActor) =>
      context.become(connected(bridges, webClients + (id -> outActor)))
    case ServerActor.Closed(id) =>
      val newWebClients = webClients.filter { case (k, _) => k != id }
      context.become(connected(bridges, newWebClients))

    // outgoing web commands
    case o: web.socket.Outgoing =>
      webClients.foreach {
        case (_, webClientActorRef) => webClientActorRef ! o
      }

    // incoming user commands
    case web.socket.Incoming.ClaimPrinter(bridge, printer, claim) =>
      log.info("Received claim {} for bridge {} and printer {}", claim, bridge, printer)
      bridges.get(bridge).foreach { bridgeActorRef =>
        bridgeActorRef ! BridgeActor.ClaimPrinter(printer, claim)
      }

    case ServerActor.Print(bridge, printer, image) =>
      log.info("Accepted print for bridge {}, printer{}", bridge, printer)
      bridges.get(bridge).foreach { bridgeActorRef =>
        bridgeActorRef ! BridgeActor.Print(printer, image)
      }
  }
}
