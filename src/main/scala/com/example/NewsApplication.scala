package com.example

import akka.actor.typed.{ActorSystem, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import com.example.behaviour.EventSourcedActor
import com.example.domain.StoryType.{Best, New, Top}
import com.example.domain._
import com.example.serialisation.CborSerializable
import com.typesafe.scalalogging.LazyLogging
import eu.timepit.refined.numeric.Interval.Closed

import scala.util.Random

object NewsApplication extends App with LazyLogging with CborSerializable {

  def apply(): Behavior[Message] = Behaviors.setup { actorContext =>

    actorContext.log.info("Creating a child 'EventSourcedActor' with name 'EventSourcedActor' ...")
    val eventSourcedActor = actorContext.spawn(EventSourcedActor(), "EventSourcedActor")

    def randomCommand: Command = {
      import eu.timepit.refined._
      import eu.timepit.refined.auto._
      val random = new Random
      val randomStoryType = StoryType.values(random.nextInt(StoryType.values.length))
      //FIXME:
      val numberOfItems: PositiveIntUpto20 = refineV[Closed[1, 20]](random.between(1,7)).getOrElse(3)
      randomStoryType match {
        case Top => FetchTopStories(numberOfItems)
        case New => FetchNewStories(numberOfItems)
        case Best => FetchBestStories(numberOfItems)
      }
    }

    Behaviors.receiveMessage { incomingMessage =>
      logger.info(s"NewsApplication received message $incomingMessage...")
      incomingMessage match {
        case FetchNews => eventSourcedActor ! randomCommand
        case Terminate => eventSourcedActor ! ShutDown
      }
      Behaviors.same
    }
  }

  logger.info("Creating the actor system ...")
  val actorSystem: ActorSystem[Message] = ActorSystem(guardianBehavior = NewsApplication(), name = "news-actor-system")

  import monix.eval._
  import monix.execution.Scheduler.Implicits.global

  // Create a list of tasks where each task sends a message
  val independentTasks = List(FetchNews, FetchNews, FetchNews, FetchNews, FetchNews)
    .map(msg => Task {actorSystem ! msg} )



  // Execute the tasks in parallel and upon completion, terminate the actor system
  Task.gather(independentTasks)
//
//    .doOnFinish(possibleError =>
//      possibleError
//        .toLeft("")
//        .fold(error => {
//          logger.error("Task to fetch news completed with error!", error)
//          Task raiseError error
//        }, _ => Task.unit)
//        .map(_=> {
//          logger.info("Terminating actor system ...")
//          actorSystem.terminate()
//        }))
//
    .runAsyncAndForget

}
