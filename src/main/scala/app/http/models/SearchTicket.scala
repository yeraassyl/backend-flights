package kz.kbtu.flights
package app.http.models

case class SearchTicket(from: String,
                        to: String,
                        dateFrom: Option[FlightDateTime],
                        dateTo: Option[FlightDateTime],
                        priceFrom: Option[Double],
                        priceTo: Option[Double])
