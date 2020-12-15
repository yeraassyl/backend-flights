package kz.kbtu.flights
package app.http.directives

import app.http.error.ApiError

import akka.http.scaladsl.server.{Directive1, Directives}

import scala.concurrent.Future
import scala.util.Success

trait TicketDirectives extends Directives{

  def handle[T](f:Future[T])(e: Throwable => ApiError): Directive1[T] = onComplete(f) flatMap {
    case Success(t) =>
      provide(t)
  }

  def handleWithGeneric[T](f: Future[T]): Directive1[T] = handle[T](f)(_ => ApiError.generic)
}
