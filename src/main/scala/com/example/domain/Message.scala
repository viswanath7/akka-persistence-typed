package com.example.domain

import cats.Show
import com.example.serialisation.CborSerializable
import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.string.NonEmptyString

sealed trait Message extends CborSerializable

final case class News(title: NonEmptyString, link: Option[String Refined Url] = None, text: Option[NonEmptyString] = None)

object News {
  import cats.syntax.option._
  def apply(title: NonEmptyString, link: String Refined Url): News = new News(title, link = link.some)
  implicit val showNews: Show[News] = Show.show(news => s"News( title: ${news.title}, information: ${news.link.map(_.value).orElse(news.text.map(_.value)).getOrElse("")})")
}

case class FetchNews(numberOfItems: PositiveIntUpto20 = 3) extends Message
case class FetchedNews(typeOfNews: StoryType, newsItems:Set[News] = Set.empty) extends Message

object FetchedNews {

  def apply(typeOfNews: StoryType, newsItems: List[News]): FetchedNews =
    new FetchedNews(typeOfNews, newsItems.toSet)

  import cats.implicits._, cats._, cats.derived
  implicit val showFetchedNews: Show[FetchedNews] = derived.semi.show
}