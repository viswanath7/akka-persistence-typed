package com.example

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.example.behaviour.EventSourcedActor
import com.example.domain.StoryType.{Best, New, Top}
import com.example.domain._
import com.example.serialisation.CborSerializable
import com.typesafe.scalalogging.LazyLogging
import monix.execution.Cancelable

import scala.language.postfixOps
import eu.timepit.refined.auto._

case class FetchNews(numberOfItems: PositiveIntUpto20 = 3)

object NewsApplication extends App with LazyLogging with CborSerializable {

  def apply(): Behavior[FetchNews] = Behaviors.setup { actorContext =>

    actorContext.log.info("Creating a child 'EventSourcedActor' with name 'EventSourcedActor' ...")
    val eventSourcedActor = actorContext.spawn(EventSourcedActor(), "EventSourcedActor")

    def randomCommand(numItems: PositiveIntUpto20): Command = {
      import scala.util.Random
      val random = new Random
      val randomStoryType = StoryType.values(random.nextInt(StoryType.values.length))
      randomStoryType match {
        case Top => FetchTopStories(numItems)
        case New => FetchNewStories(numItems)
        case Best => FetchBestStories(numItems)
      }
    }

    Behaviors.receiveMessage { incomingMessage =>
      val command = randomCommand(incomingMessage.numberOfItems)
      logger.info(s"Sending command '$command' to event sourced actor with path '${eventSourcedActor.path}' ...")
      eventSourcedActor ! command
      Behaviors.same
    }
  }

  logger.info("Creating the actor system 'news-actor-system' ...")
  val actorSystem: ActorSystem[FetchNews] = ActorSystem(guardianBehavior = NewsApplication(), name = "news-actor-system")

  def sendMessages(messages: Seq[FetchNews]): Cancelable = {
    import monix.execution.Cancelable
    import monix.execution.Scheduler.{global => scheduler}
    import scala.concurrent.duration._
    @scala.annotation.tailrec
    def sendMessageToActor(messagesToActor: Seq[FetchNews]): Cancelable = messagesToActor match {
         case Nil =>
           Thread.sleep(10000)
           logger.info("No more messages to send")
           logger.info("Terminating actor system ...")
           Cancelable(() => actorSystem.terminate())
         case head :: tail =>
           logger.info(s"Sending message $head")
           scheduler.scheduleOnce( 2 second) (actorSystem ! head)
           sendMessageToActor(tail)
       }
    sendMessageToActor(messages)
  }

  sendMessages(List(FetchNews(), FetchNews(), FetchNews(), FetchNews(), FetchNews()))
}
