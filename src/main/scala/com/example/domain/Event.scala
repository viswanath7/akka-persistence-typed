package com.example.domain

import java.time.LocalDate
import cats.data.NonEmptyList

// Types of events the persistence actor is allowed to emit when processing a successfully validated command
sealed trait Event {
  def date:LocalDate
  def stories: NonEmptyList[News]
}
final case class TopStoriesFetched(date: LocalDate, stories: NonEmptyList[News]) extends Event
final case class NewStoriesFetched(date: LocalDate, stories: NonEmptyList[News]) extends Event
final case class BestStoriesFetched(date: LocalDate, stories: NonEmptyList[News]) extends Event