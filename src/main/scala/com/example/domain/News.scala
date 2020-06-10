package com.example.domain

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.string.NonEmptyString

final case class News(title: NonEmptyString, link: Option[String Refined Url] = None, text: Option[NonEmptyString] = None)

object News {
  import cats.syntax.option._
  def apply(title: NonEmptyString, link: String Refined Url): News = new News(title, link = link.some)
}
