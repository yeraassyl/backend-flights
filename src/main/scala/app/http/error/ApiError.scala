package kz.kbtu.flights
package app.http.error

import akka.http.scaladsl.model.{StatusCode, StatusCodes}

object ApiError {
  private def apply(statusCode: StatusCode, message: String) = new ApiError(statusCode, message)

  val generic: ApiError = new ApiError(StatusCodes.InternalServerError, "Unknown error.")

  val emptyUserIdField = new ApiError(StatusCodes.BadRequest, "The userId field must not be empty")
  val emptyTicketIdField = new ApiError(StatusCodes.BadRequest, "The ticketId field must not be empty")
  val unauthorized = new ApiError(StatusCodes.Unauthorized, "Unauthorized checkout")

  def ticketNotFound(id: String): ApiError = new ApiError(StatusCodes.NotFound, s"The ticket with id $id could not be found")
}

final case class ApiError private(statusCode: StatusCode, message: String)
