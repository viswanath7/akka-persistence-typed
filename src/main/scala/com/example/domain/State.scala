package com.example.domain

import java.time.LocalDate

import cats.data.NonEmptyList
import com.example.serialisation.CborSerializable

sealed trait State extends CborSerializable

case object EmptyState extends State

final case class PopulatedState(data:Map[(StoryType, LocalDate), NonEmptyList[News]] = Map.empty) extends State {
  def withLatestNews(storyType: StoryType, localDate: LocalDate, news: NonEmptyList[News]): PopulatedState = {
    copy(data = data.filterNot(entry => entry._1._1 == storyType) + ( (storyType, localDate) -> news ) )
  }
}