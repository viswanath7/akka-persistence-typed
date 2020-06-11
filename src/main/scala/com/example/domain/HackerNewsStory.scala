package com.example.domain

import cats.syntax.either._
import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{NonNegative, Positive}
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.string.NonEmptyString
import io.circe.{Decoder, DecodingFailure, HCursor}

final case class HackerNewsStory(by:NonEmptyString,
                                 descendants: Int Refined NonNegative = 0,
                                 id: Long,
                                 kids: List[Int] = List.empty,
                                 score: Int,
                                 time: Int Refined Positive,
                                 title:NonEmptyString,
                                 `type`: NonEmptyString,
                                 url: Option[String Refined Url] = None,
                                 text: Option[NonEmptyString] = None)

object HackerNewsStory {
  import io.circe.refined._
  import eu.timepit.refined.auto._
  implicit val decoder: Decoder[HackerNewsStory] = {
    (hCursor: HCursor) => for {
      by <- hCursor.downField("by").as[NonEmptyString]
      descendants <- hCursor.downField("descendants").as[Int Refined NonNegative]
      id <- hCursor.downField("id").as[Long]
      kids <- hCursor.downField("kids").as[List[Int]].orElse(List.empty[Int].asRight[DecodingFailure])
      score <- hCursor.downField("score").as[Int]
      time <- hCursor.downField("time").as[Int Refined Positive]
      title <- hCursor.downField("title").as[NonEmptyString]
      sort <- hCursor.downField("type").as[NonEmptyString]
      url <- hCursor.downField("url").as[Option[String Refined Url]].orElse(None.asRight[DecodingFailure])
      text <- hCursor.downField("text").as[Option[NonEmptyString]].orElse(None.asRight[DecodingFailure])
    } yield {
      new HackerNewsStory(by, descendants, id, kids, score, time, title, sort, url, text)
    }
  }
}