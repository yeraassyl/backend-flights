package kz.kbtu.flights
package app.http.repository

import app.http.models.{SearchTicket, Ticket}

import scala.concurrent.{ExecutionContext, Future}

trait TicketRepository{
  def all(): Future[Seq[Ticket]]
  def get(id: String): Future[Ticket]
  def search(searchTicket: SearchTicket): Future[Seq[Ticket]]
}

object TicketRepository{
  final case class TicketNotFound(id: String) extends Exception("")
}

class InMemoryTicketRepository(initialTickets: Seq[Ticket] = Seq.empty)(implicit ec: ExecutionContext) extends TicketRepository {
  import TicketRepository._

  private var tickets = initialTickets.toVector
  
  override def all(): Future[Seq[Ticket]] = Future.successful(tickets)

  override def get(id: String): Future[Ticket] = tickets.find(_.id == id) match {
    case Some(foundTicket) =>
      Future.successful(foundTicket)
    case None =>
      Future.failed(TicketNotFound(id))
  }
  override def search(searchTicket: SearchTicket): Future[Seq[Ticket]] = Future.successful {
    var _tickets = tickets.filter(ticket => ticket.from == searchTicket.from && ticket.to == searchTicket.to)
    _tickets = searchTicket.dateFrom.fold(_tickets)(from => _tickets.filter(t => from <= t.date))
    _tickets = searchTicket.dateTo.fold(_tickets)(to => _tickets.filter(t => to > t.date))
    _tickets = searchTicket.priceFrom.fold(_tickets)(from => _tickets.filter(t => from <= t.price))
    searchTicket.priceTo.fold(_tickets)(to => _tickets.filter(t => to > t.price))
  }
}
