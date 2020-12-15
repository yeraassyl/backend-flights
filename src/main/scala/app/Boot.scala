package kz.kbtu.flights
package app

import app.actors.FlightsSupervisor
import app.http.models.{FlightDateTime, Ticket}
import app.http.repository.InMemoryTicketRepository
import app.http.router.Router
import app.http.server.Server

import akka.actor.typed.ActorSystem

import java.text.SimpleDateFormat
import scala.concurrent.ExecutionContext

object Boot extends App {
  val dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm")

  val tickets = Seq(
    Ticket("41234", "ALA", "TSE", 5000, FlightDateTime(10, 5, 2020)),
    Ticket("54134", "TSE", "ALA", 10000, FlightDateTime(15, 8, 2020)),
    Ticket("09094", "ALA", "TSE", 15000, FlightDateTime(20, 10, 2020))
  )

  val host = "localhost"
  val port = 8080

  implicit val flightsSupervisor: ActorSystem[FlightsSupervisor.Command] = ActorSystem(FlightsSupervisor(), "flights-supervisor")

  implicit val ec: ExecutionContext = flightsSupervisor.executionContext

  val ticketsRepository = new InMemoryTicketRepository(tickets)

  val router: Router = new Router(ticketsRepository)

  Server.startHttpServer(router.route, host, port)
}
