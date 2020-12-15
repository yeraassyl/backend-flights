package kz.kbtu.flights
package app.http.models

case class FlightDateTime(day: Int, month: Int, year: Int) extends Ordered[FlightDateTime] {

  override def compare(that: FlightDateTime): Int = {
    if (this.year > that.year) {
      1
    } else if (this.year < that.year) {
      -1
    } else {
      if (this.month > this.month) {
        1
      } else if (this.month < this.month) {
        -1
      } else {
        this.day.compare(that.day)
      }
    }
  }

  override def toString: String = {
    s"$day/$month/$year"
  }
}
