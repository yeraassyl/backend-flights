package kz.kbtu.flights
package app.actors

import app.actors.FlightsSupervisor.BuyTicket
import app.actors.User.{AddTicket, TicketList}
import app.actors.UserManager.Command

import akka.actor.typed._
import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.util.Timeout

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

object UserManager{
  def apply(): Behavior[Command] = Behaviors.setup(context => new UserManager(context))

  trait Command
  case class GetTicketList(userId: String, replyTo: ActorRef[TicketList]) extends Command
  case class Checkout(userId: String) extends Command
}

class UserManager(context: ActorContext[Command]) extends AbstractBehavior[Command](context){
  import UserManager._
  implicit val ec: ExecutionContext = context.executionContext
  val system: ActorSystem[Nothing] = context.system
  implicit val scheduler: Scheduler = system.scheduler
  implicit val timeout: Timeout = 3.seconds

  var userIdToActor = Map.empty[String, ActorRef[User.Command]]

  override def onMessage(msg: Command): Behavior[Command] = {
    msg match {
      case BuyTicket(id, ticketId) =>
        userIdToActor.get(id) match {
          case Some(ref) =>
            ref ! AddTicket(ticketId)
          case None =>
            context.log.info(s"Creating user actor with id: $id")
            val userActor = context.spawn(User(id), "id-" + id)
            context.watchWith(userActor, Checkout(id))
            userActor ! AddTicket(ticketId)
            userIdToActor += id -> userActor
        }
        this
      case GetTicketList(userId, replyTo) =>
        userIdToActor.get(userId) match {
          case Some(ref) =>
            (ref ? User.GetTicketList).onComplete(ticketList => replyTo ! ticketList.get)
          case None =>
            replyTo ! TicketList(Seq.empty)
            context.log.error(s"No user with id : $userId")
        }
        this
      case Checkout(id) =>
        context.log.info(s"User with id $id is leaving")
        userIdToActor -= id
        this
    }
  }

  override def onSignal: PartialFunction[Signal, Behavior[Command]] = {
    case PostStop =>
      context.log.info("UserManager stopped")
      this
  }
}
