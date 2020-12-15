package kz.kbtu.flights
package app.actors

import app.actors.User.Command

import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, PostStop, Signal}

object User{
  def apply(id: String): Behavior[Command] =
    Behaviors.setup(context => new User(context, id))

  trait Command
  case class AddTicket(ticketId: String) extends Command
  case class GetTicketList(replyTo: ActorRef[TicketList]) extends Command

  case class TicketList(tickets: Seq[String])
}

class User(context: ActorContext[Command], id: String) extends AbstractBehavior[Command](context){
  import User._

  var ticketList = Seq.empty[String]
  context.log.info(s"User actor started with id: $id")

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case AddTicket(ticket) =>
        ticketList = ticketList :+ ticket
        context.log.info(s"Ticket ${ticket} added")
        this
      case GetTicketList(replyTo) =>
        context.log.info(s"Getting ticket list")
        replyTo ! TicketList(ticketList)
        this
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info(s"User actor $id stopped")
      this
  }
}
