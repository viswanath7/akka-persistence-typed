package com.example.domain

import cats.Show
import enumeratum._
import eu.timepit.refined.types.string.NonEmptyString
import com.example._

sealed trait StoryType extends EnumEntry {
  def urlPath: NonEmptyString
}

object StoryType extends Enum[StoryType] with CirceEnum[StoryType] {
  val values = findValues

  case object Top   extends StoryType {
    override def urlPath: NonEmptyString = "/topstories.json?print=pretty".toNonEmptyString
  }
  case object New   extends StoryType {
    override def urlPath: NonEmptyString = "/newstories.json?print=pretty".toNonEmptyString
  }
  case object Best  extends StoryType {
    override def urlPath: NonEmptyString = "/beststories.json?print=pretty".toNonEmptyString
  }

  implicit val showStoryType: Show[StoryType] = Show.show(storyType => storyType.entryName)
}