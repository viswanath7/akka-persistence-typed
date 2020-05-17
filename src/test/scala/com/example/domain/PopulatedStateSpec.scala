package com.example.domain

import java.time.LocalDate

import cats.data.NonEmptyList
import com.example.domain.StoryType.{Best, New}
import eu.timepit.refined.api.Refined
import eu.timepit.refined.auto._
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.string.NonEmptyString
import org.scalatest.OptionValues
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers


class PopulatedStateSpec extends AnyFlatSpecLike with Matchers with OptionValues{

  "State case class" should "retain only one set of news per story type for latest date" in {

    var state = PopulatedState()

    val firstNewsTitle: NonEmptyString = "new-news-1"
    val firstNewsURL: String Refined Url = "http://example.com/breaking-news/1"
    state = PopulatedState().withLatestNews(New, LocalDate.now(), NonEmptyList of News(firstNewsTitle, firstNewsURL))
    state.data should contain key ((New,LocalDate.now()))
    state.data should contain value NonEmptyList.of(News(firstNewsTitle, firstNewsURL))

    val secondNewsTitle: NonEmptyString = "new-news-2"
    val secondNewsURL: String Refined Url = "http://example.com/breaking-news/2"
    val bestNews = NonEmptyList.of(News(secondNewsTitle, secondNewsURL))
    state = state.withLatestNews(Best, LocalDate.now(), bestNews)
    state.data should have size 2
    state.data.get((Best, LocalDate.now())).value shouldBe bestNews

    val thirdNewsTitle: NonEmptyString = "new-news-3"
    val thirdNewsURL: String Refined Url = "http://example.com/breaking-news/3"
    val overwrittenBestNews = NonEmptyList.of(News(thirdNewsTitle, thirdNewsURL))
    state = state.withLatestNews(Best, LocalDate.now(), overwrittenBestNews)
    state.data should have size 2
    state.data.get((Best, LocalDate.now())).value shouldBe overwrittenBestNews
  }

}
