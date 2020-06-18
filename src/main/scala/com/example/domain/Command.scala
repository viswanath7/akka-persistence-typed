package com.example.domain

import akka.actor.typed.ActorRef
import com.example.serialisation.CborSerializable
import eu.timepit.refined.auto._

// Types of messages the persistence actor is allowed to receive
sealed trait Command extends CborSerializable
case object ShutDown extends Command

sealed trait StoryCommand {
  self: Command =>
  def numberOfItems: PositiveIntUpto20
  def sender: ActorRef[FetchedNews]
}

final case class FetchTopStories(numberOfItems: PositiveIntUpto20 = 3, sender: ActorRef[FetchedNews]) extends StoryCommand with Command
final case class FetchNewStories(numberOfItems: PositiveIntUpto20 = 3, sender: ActorRef[FetchedNews]) extends StoryCommand with Command
final case class FetchBestStories(numberOfItems: PositiveIntUpto20 = 3, sender: ActorRef[FetchedNews]) extends StoryCommand with Command
