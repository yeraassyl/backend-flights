package kz.kbtu.flights
package app.http.router

import app.actors.FlightsSupervisor
import app.actors.FlightsSupervisor.GetUserTickets
import app.actors.User.TicketList
import app.http.directives.TicketDirectives
import app.http.directives.validator.{BuyTicketValidator, CheckoutValidator, ValidatorDirective}
import app.http.models._
import app.http.repository.TicketRepository

import akka.actor.typed.scaladsl.AskPattern.Askable
import akka.actor.typed.{ActorSystem, Scheduler}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Directives, Route}
import akka.util.Timeout
import spray.json.{DefaultJsonProtocol, JsArray, JsString, JsValue, RootJsonFormat}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {
  implicit object dateTimeFormat extends RootJsonFormat[FlightDateTime] {
    override def write(obj: FlightDateTime): JsValue = {
      JsString(obj.toString)
    }

    override def read(json: JsValue): FlightDateTime = {
      val str = json.toString
      val date: Array[Int] = str.split("/").map(value => value.toInt)
      FlightDateTime(date(0), date(1), date(2))
    }
  }

  implicit object ticketListFormat extends RootJsonFormat[TicketList] {
    override def read(json: JsValue): TicketList = TicketList(Seq.empty)

    override def write(obj: TicketList): JsValue = {
      val jsArray: Vector[JsString] = obj.tickets.map(ticket => JsString(ticket)).toVector
      JsArray(jsArray)
    }
  }

  implicit val ticketFormat: RootJsonFormat[Ticket] = jsonFormat5(Ticket)
  implicit val buyTicketFormat: RootJsonFormat[BuyTicket] = jsonFormat2(BuyTicket)
  implicit val checkoutFormat: RootJsonFormat[Checkout] = jsonFormat1(Checkout)
}

class Router(ticketRepository: TicketRepository)(implicit system: ActorSystem[FlightsSupervisor.Command]) extends Directives with TicketDirectives with ValidatorDirective with JsonSupport {
  val supervisor: ActorSystem[FlightsSupervisor.Command] = system
  implicit val timeout: Timeout = 5.seconds
  implicit val scheduler: Scheduler = system.scheduler
  implicit val ec: ExecutionContext = system.executionContext

  def route: Route = {
    pathPrefix("tickets") {
      get {
        parameters(
          "from",
          "to",
          "date_from".as[FlightDateTime].optional,
          "date_to".as[FlightDateTime].optional,
          "price_from".as[Double].optional,
          "price_to".as[Double].optional).as(SearchTicket) { searchTicket: SearchTicket =>
          handleWithGeneric(ticketRepository.search(searchTicket)) {
            tickets => complete(tickets)
          }
        }
      } ~ path(Segment) { id: String =>
        get {
          handleWithGeneric(ticketRepository.get(id)) { ticket =>
            complete(ticket)
          }
        }
      } ~ pathSingleSlash {
        get {
          handleWithGeneric(ticketRepository.all()) { tickets =>
            complete(tickets)
          }
        } ~ post {
          entity(as[BuyTicket]) { buyTicket =>
            validateWith(BuyTicketValidator)(buyTicket) {
              onComplete(ticketRepository.get(buyTicket.ticketId)) {
                case Success(_) =>
                  supervisor ! FlightsSupervisor.BuyTicket(buyTicket.userId, buyTicket.ticketId)
                  complete(StatusCodes.Accepted, "ticket added")
                case Failure(exception) =>
                  complete(StatusCodes.NotFound, "ticket not found")
              }
            }
          }
        }
      }
    } ~ pathPrefix("user") {
      path(Segment) { id: String =>
        get {
          val tickets = (supervisor ? (ref => GetUserTickets(id, ref))).mapTo[TicketList]
          handleWithGeneric(tickets) { ticketList =>
            if (ticketList.tickets.nonEmpty){
              complete(StatusCodes.OK, ticketList)
            } else {
              complete(StatusCodes.NotFound, s"there is no user with id: $id")
            }
          }
        }
      }
    } ~ pathPrefix("checkout") {
      post {
        entity(as[Checkout]) { checkout =>
          validateWith(CheckoutValidator)(checkout) {
            val tickets = (supervisor ? (ref => FlightsSupervisor.Checkout(checkout.userId, ref))).mapTo[TicketList]
            onComplete(tickets) {
              case Success(value) =>
                val sum = value.tickets.foldLeft(0.0)((a, b) => a + ticketRepository.get(b).value.get.get.price)
                complete(StatusCodes.Accepted, s"Your total sum is $sum")
              case Failure(exception) =>
                complete(StatusCodes.InternalServerError, "couldn't retrieve tickets")
            }
            complete(StatusCodes.Accepted, "thank you")
          }
        }
      }
    }
  }
}
