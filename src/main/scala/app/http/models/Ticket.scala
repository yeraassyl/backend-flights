package kz.kbtu.flights
package app.http.models

case class Ticket(id: String, from: String, to: String, price: Double, date: FlightDateTime)
