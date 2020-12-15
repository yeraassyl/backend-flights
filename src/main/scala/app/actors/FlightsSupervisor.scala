package kz.kbtu.flights
package app.actors

import app.actors.FlightsSupervisor.Command
import app.actors.User.TicketList
import app.actors.UserManager.GetTicketList

import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object FlightsSupervisor {
  def apply(): Behavior[Command] = Behaviors.setup[Command](context => new FlightsSupervisor(context))

  sealed trait Command

  case class BuyTicket(userId: String, ticketId: String) extends Command with UserManager.Command

  case class GetUserTickets(userId: String, replyTo: ActorRef[TicketList]) extends Command

  case class Checkout(userId: String, replyTo: ActorRef[TicketList]) extends Command with UserManager.Command
}

class FlightsSupervisor(context: ActorContext[Command]) extends AbstractBehavior[Command](context) {

  import FlightsSupervisor._

  val system: ActorSystem[Nothing] = context.system
  implicit val ec: ExecutionContext = context.executionContext
  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = 3.seconds

  val userManager: ActorRef[UserManager.Command] = context.spawn(UserManager(), "user-manager")
  val ticketManager: ActorRef[TicketManager.Command] = context.spawn(TicketManager(), "ticket-manager")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case buyTicketMsg@BuyTicket(_, _) =>
        context.log.info("Forwarding message to the UserManager Actor")
        userManager ! buyTicketMsg
        this
      case GetUserTickets(userId, replyTo) =>
        context.log.info("Getting user tickets from the UserManager Actor")
        (userManager ? (ref => GetTicketList(userId, ref))).onComplete(ticketList => replyTo ! ticketList.get)
        this
      case Checkout(userId, replyTo) =>
        context.log.info("Forwarding checkout message to the UserManager Actor")
        (userManager ? (ref => GetTicketList(userId, ref))).onComplete(ticketList => replyTo ! ticketList.get)
        userManager ! UserManager.Checkout(userId)
        this
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info("Flights app stopped")
      this
  }

}
