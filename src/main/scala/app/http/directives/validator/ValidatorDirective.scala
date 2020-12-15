package kz.kbtu.flights
package app.http.directives.validator

import akka.http.scaladsl.server.{Directive0, Directives}

trait ValidatorDirective extends Directives{
  def validateWith[T](validator: Validator[T])(t: T): Directive0 =
    validator.validate(t) match {
      case Some(apiError) =>
        complete(apiError.statusCode, apiError.message)
      case None =>
        pass
    }
}
