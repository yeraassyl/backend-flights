package kz.kbtu.flights
package app.http.directives.validator

import app.http.error.ApiError
import app.http.models.{BuyTicket, Checkout}

trait Validator[T] {
  def validate(t: T): Option[ApiError]
}

object BuyTicketValidator extends Validator[BuyTicket]{
  override def validate(t: BuyTicket): Option[ApiError] =
    if (t.userId.isEmpty)
      Some(ApiError.emptyUserIdField)
    else if (t.ticketId.isEmpty)
      Some(ApiError.emptyTicketIdField)
    else
      None
}

object CheckoutValidator extends Validator[Checkout] {
  override def validate(t: Checkout): Option[ApiError] = {
    if (t.userId.isEmpty){
      Some(ApiError.unauthorized)
    } else {
      None
    }
  }
}