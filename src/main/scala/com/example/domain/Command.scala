package com.example.domain

import eu.timepit.refined.auto._

// Types of messages the persistence actor is allowed to receive
sealed trait Command {
  def numberOfItems: PositiveIntUpto20
}
final case class FetchTopStories(numberOfItems: PositiveIntUpto20 = 3) extends Command
final case class FetchNewStories(numberOfItems: PositiveIntUpto20 = 3) extends Command
final case class FetchBestStories(numberOfItems: PositiveIntUpto20 = 3) extends Command
