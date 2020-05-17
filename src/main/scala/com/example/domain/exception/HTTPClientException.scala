package com.example.domain.exception

import io.circe.Error
import sttp.client.ResponseError

case class HTTPClientException(body:String, rootCause:Throwable) extends Exception

object HTTPClientException {
  def apply(error: ResponseError[Error]): HTTPClientException = new HTTPClientException(error.body, error.getCause)
}
