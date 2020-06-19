package com.example.service
import cats.effect.IO
import com.example.domain.{HackerNewsStory, PositiveIntUpto20, StoryType}
import org.scalacheck.ScalacheckShapeless._
import eu.timepit.refined.scalacheck.all._
import com.example.Generator._

object MockNewsService extends NewsService with App {
  override def retrieveStories(storyType: StoryType)(numberOfItems: PositiveIntUpto20): IO[List[HackerNewsStory]] = IO {
    val hackNewsStories = random[List[HackerNewsStory]]
    if(hackNewsStories.length>numberOfItems.value) hackNewsStories.take(numberOfItems.value) else hackNewsStories
  }
}
