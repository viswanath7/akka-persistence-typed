package com.example.behaviour

import java.time.LocalDate
import cats.data.NonEmptyList
import com.example.domain.{News, TopStoriesFetched}
import eu.timepit.refined.auto._
import org.scalatest.flatspec.AsyncFlatSpecLike
import org.scalatest.matchers.should.Matchers

class PairOpsSpec extends AsyncFlatSpecLike with Matchers{

  "A pair of local date and non empty list of news" should "be converted to an event type" in {
    val localDate = LocalDate.now()
    val stories: NonEmptyList[News] = NonEmptyList.of( News("Sample tile", link = "https://bbc.co.uk/news") )
    val fetched: TopStoriesFetched = (localDate, stories).toCaseClass[TopStoriesFetched]
    fetched.date shouldBe localDate
    fetched.stories shouldBe stories
  }

}
