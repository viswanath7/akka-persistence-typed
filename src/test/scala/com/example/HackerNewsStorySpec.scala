package com.example

import com.example.domain.HackerNewsStory
import eu.timepit.refined.auto._
import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{NonNegative, Positive}
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.string.NonEmptyString
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

class HackerNewsStorySpec extends AnyFlatSpecLike with Matchers {

  "A case class instance" should "be created successfully" in {
    val by: NonEmptyString = "dhouston"
    val descendants: Int Refined NonNegative = 71
    val id = 8863
    val kids = List(8952, 9224, 8917, 8884)
    val score = 111
    val time: Int Refined Positive = 1175714200
    val title: NonEmptyString = "My YC app: Dropbox - Throw away your USB drive"
    val `type`:NonEmptyString = "story"
    val url: String Refined Url = "http://www.getdropbox.com/u/2/screencast.html"
    val hackerNewsStory = HackerNewsStory(by, descendants, id, kids, score, time, title, `type`, url)
    hackerNewsStory should have (
      Symbol("title") (title.value),
      Symbol("url") (url.value)
    )

    import io.circe.refined._
    import eu.timepit.refined.auto._
    import io.circe.generic.auto._, io.circe.syntax._
    hackerNewsStory.asJson.noSpaces shouldBe
    """{"by":"dhouston","descendants":71,"id":8863,"kids":[8952,9224,8917,8884],"score":111,"time":1175714200,"title":"My YC app: Dropbox - Throw away your USB drive","type":"story","url":"http://www.getdropbox.com/u/2/screencast.html"}"""
  }

}
