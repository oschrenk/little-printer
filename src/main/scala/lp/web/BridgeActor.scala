package lp.web

import java.awt.image.BufferedImage

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import lp.hardware.Claiming
import lp.hardware.protocol.in._
import lp.hardware.protocol.in.bridge.{DeviceConnect, DeviceDisconnect, EncryptionKeyRequired, PowerOn, Event => BridgeSubEvent}
import lp.hardware.protocol.in.printer.{DeviceDidPowerOn, DeviceHeartbeat}
import lp.hardware.protocol.out.bridge.{AddDeviceEncryptionKey, Command => BridgeSubCommand}
import lp.hardware.protocol.out.printer.{Command, SetDeliveryAndPrint}
import lp.hardware.protocol.out.{BridgeCommand, DeviceCommand}
import lp.image.{Monochrome, Transformations}
import lp.model.{Bridge, Claim, Printer}
import lp.web
import lp.web.BridgeActor.{ClaimPrinter, Connected, Disconnected, Print}

import scala.util.Random

object BridgeActor {
  def props(serverActor: ActorRef): Props = Props(new BridgeActor(serverActor))

  // manage actors for bridge-facing websocket connections
  case class Connected(outActor: ActorRef)
  case object Disconnected

  // manage commands from user via server actor
  case class Print(deviceAddress: Printer.Address, image: BufferedImage)
  case class ClaimPrinter(deviceAddress: Printer.Address, claim: Claim)
}

trait Default extends ActorLogging {
  this: BridgeActor =>
  val default: Receive = {
    case b: BridgeLog =>
      log.info(s"$b")
    case bcr: BridgeCommandResponse =>
      log.info(bcr.toString)
    case dcr: DeviceCommandResponse =>
      log.info(dcr.toString)
    case m =>
      log.info("Unhandled {}", m)
  }
}

class BridgeActor(val serverActor: ActorRef) extends Actor with Default {

  override def receive: Receive = {
    case Connected(outActor) =>
      context.become(connected(outActor).orElse(default))
  }

  def connected(outActor: ActorRef): Receive = {
    case be @ BridgeEvent(address, _, json) =>
      BridgeSubEvent.parse(json) match {
        case Left(m) =>
          log.error(m)
        case Right(event) =>
          event match {
            case _: PowerOn =>
              serverActor ! ServerActor.Connected(address, self)
              context.become(
                withBridge(outActor, Bridge(address, Set.empty)).orElse(default)
              )
            case _ =>
              serverActor ! ServerActor.Connected(address, self)
              // bridge does no re-send `power_on` on connect, so we capture the
              // first bridge event, assume the given bridge address is the
              // connected bridge, change context, and re-send the message
              self ! be
              context.become(
                withBridge(outActor, Bridge(address, Set.empty)).orElse(default)
              )
          }
      }
  }

  def withBridge(outActor: ActorRef, bridge: Bridge): Receive = {
    // we assume that every bridge event comes from the same bridge, since it
    // it is the one that opened the connection
    case BridgeEvent(_, _, json) =>
      BridgeSubEvent.parse(json) match {
        case Left(m) => log.error(m)
        case Right(event) =>
          event match {
            case _: PowerOn          => // ignore
            case DeviceConnect(_)    => // bridge.markAlive(deviceAddress)
            case DeviceDisconnect(_) => // bridge.markDead(deviceAddress)
            case EncryptionKeyRequired(deviceAddress) =>
              log.info("Encryption key required for {}", deviceAddress)
              serverActor ! web.socket.Outgoing.UnclaimedPrinter(bridge.address, deviceAddress)
          }
      }
    case de @ DeviceEvent(_, deviceAddress, _, _, payload) =>
      printer.Decoder.fromBase64(payload) match {
        case Left(m) =>
          log.error("Error: {} with event `{}`", m, de)
        case Right(e) =>
          e match {
            case _: DeviceDidPowerOn =>
              log.info("DeviceDidPowerOn for {}.", deviceAddress)
              serverActor ! web.socket.Outgoing.Heartbeat(bridge.address, deviceAddress)
            case DeviceHeartbeat(uptime) =>
              log.info("Heartbeat for {}. Alive for {} seconds.", deviceAddress, uptime)
              serverActor ! web.socket.Outgoing.Heartbeat(bridge.address, deviceAddress)
            case e =>
              log.info("Unhandled device event {}.", e)
          }
      }

    // manage commands from user via server actor
    case Print(printer, image) =>
      log.info("printing for {}", printer)
      sendDeviceCommand(outActor, bridge, printer, image)

    case ClaimPrinter(printer, claim) =>
      Claiming.process(claim) match {
        case Left(e) =>
          log.error("Claiming process failed. Reason: {}", e.getMessage)
        case Right((_, key)) =>
          sendBridgeCommand(
            outActor,
            bridge,
            AddDeviceEncryptionKey(printer, key)
          )
      }

    // discard all messages after this
    case Disconnected =>
      serverActor ! ServerActor.Disconnected(bridge.address)
      context.stop(self)
  }

  private def sendDeviceCommand(
      actor: ActorRef,
      bridge: Bridge,
      deviceAddress: Printer.Address,
      image: BufferedImage
  ): Unit = {
    // we could manage commands and commands ids in a queue
    // for now it is fine to fire and forget
    val commandId = Random.nextInt(10000)
    val timestamp = System.currentTimeMillis().toFloat / 1000
    val mono = Monochrome.dither(Transformations.rotate180(image))
    Command.encode(SetDeliveryAndPrint(commandId, mono)).foreach { payload =>
      actor ! DeviceCommand(
        bridge.address,
        deviceAddress,
        commandId,
        timestamp,
        payload
      )
    }
  }

  private def sendBridgeCommand(
      actor: ActorRef,
      bridge: Bridge,
      cmd: BridgeSubCommand
  ): Unit = {
    val payload = BridgeSubCommand.encode(cmd)
    val commandId = Random.nextInt(10000)
    val timestamp = System.currentTimeMillis().toFloat / 1000
    actor ! BridgeCommand(
      bridge.address,
      commandId,
      timestamp,
      payload
    )
  }
}
