package kz.kbtu.flights
package app.actors

import app.actors.TicketManager.Command

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext}
import akka.actor.typed.scaladsl.Behaviors

object TicketManager{
  def apply(): Behavior[Command] = Behaviors.setup(context => new TicketManager(context))

  sealed trait Command
  final case class GetTicketList()
}

class TicketManager(context: ActorContext[Command]) extends AbstractBehavior[Command](context){
  import TicketManager._

  override def onMessage(msg: Command): Behavior[Command] = {
    this
  }
}
