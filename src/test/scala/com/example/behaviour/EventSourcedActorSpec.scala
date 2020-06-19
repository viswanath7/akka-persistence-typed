package com.example.behaviour

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.{ScalaTestWithActorTestKit, TestProbe}
import akka.actor.typed.ActorRef
import com.example.domain._
import com.example.service.MockNewsService
import eu.timepit.refined.auto._
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers

import scala.language.postfixOps

class EventSourcedActorSpec extends ScalaTestWithActorTestKit(config=s"""
      akka.persistence.journal.plugin = "akka.persistence.journal.inmem"
      akka.persistence.snapshot-store.plugin = "akka.persistence.snapshot-store.local"
      akka.persistence.snapshot-store.local.dir = "target/snapshot-${UUID.randomUUID().toString}"
    """) with AnyFlatSpecLike with Matchers {

  "EventSourcedActor" must "handle FetchTopStories command" in {
    // Create eventSourcedActor as the child of test kit; which is the guardian actor
    val eventSourcedActor: ActorRef[Command] = testKit.spawn(EventSourcedActor.createWith(MockNewsService))
    val testProbe: TestProbe[FetchedNews] = testKit.createTestProbe[FetchedNews]
    eventSourcedActor ! FetchTopStories(5, testProbe.ref)
    val fetchedNews = testProbe.receiveMessage()
    fetchedNews.typeOfNews.entryName shouldBe "Top"
    fetchedNews.newsItems should have size 5
  }

  "EventSourcedActor" must "terminate when receiving Shutdown command" in {
    val eventSourcedActor: ActorRef[Command] = testKit.spawn(EventSourcedActor.createWith(MockNewsService))
    val testProbe: TestProbe[FetchedNews] = testKit.createTestProbe[FetchedNews]
    eventSourcedActor ! ShutDown
    testProbe.expectTerminated(eventSourcedActor)
  }
}
