package com.example.behaviour

import java.time.LocalDate

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import cats.data.NonEmptyList
import cats.effect.IO
import com.example.domain.StoryType.{Best, New, Top}
import com.example.domain.{StoryCommand, _}
import com.example.serialisation.CborSerializable
import com.example.service.NewsService
import com.typesafe.scalalogging.LazyLogging
import shapeless.LabelledGeneric

object EventSourcedActor extends LazyLogging with CborSerializable {

  /**
   * Generates a stable unique identifier for the persistent actor
   * in the backend event journal and snapshot store
   *
   * @return
   */
  lazy val createPersistenceIdentifier: PersistenceId = {
    import io.chrisdavenport.fuuid.FUUID
    FUUID.randomFUUID[IO]
      .map(functionalUUID => PersistenceId ofUniqueId functionalUUID.show).unsafeRunSync()
  }

  /**
   * State when the entity is first created
   *
   * @return empty state
   */
  lazy val initialState: State = EmptyState

  def createWith(newsService: NewsService): Behavior[Command] = {
    import akka.persistence.typed.scaladsl.EventSourcedBehavior

    /**
     * Defines how to handle an incoming command given the current state, by producing an Effect.
     * An effect is a directive that defines event(s) to persist if any
     *
     * @return A function that accepts current state and incoming command to return an Effect.
     */
    def onCommand: (State, Command) => Effect[Event, State] = {

      (currentState, incomingCommand) => {
        logger.info(s"Processing command $incomingCommand. Current state: $currentState")
        def handleIncomingCommand: Effect[Event, State] = {

          def processCommand[E <: Event](storyType: StoryType, command: StoryCommand, fn: PositiveIntUpto20 => IO[List[HackerNewsStory]] )
                                        (implicit labelledGeneric: LabelledGeneric[E]): Effect[Event, State] = {
            def fetchNews: List[News] = fn(command.numberOfItems).unsafeRunSync().map(hns => News(hns.title, hns.url, hns.text))
            val news = fetchNews
            logger info s"Fetched news ${news
              .map{ n=> s"Title: ${n.title},  Detail: ${n.link.getOrElse(n.title)}"  }
              .mkString("\n", "\n", "\n")}"
            if(news.isEmpty)
              Effect.none.thenReply(command.sender)(_ => FetchedNews(storyType))
            else {
              val event = (LocalDate.now(), NonEmptyList fromListUnsafe news).toCaseClass[E]
              Effect.persist(event).thenReply(command.sender)( _ => FetchedNews(storyType, news) )
            }
          }

          incomingCommand match {
            case ShutDown => Effect.stop().thenNoReply()
            case cmd:FetchTopStories => processCommand[TopStoriesFetched](Top, cmd, newsService.retrieveStories(Top))
            case cmd:FetchNewStories => processCommand[NewStoriesFetched](New, cmd, newsService.retrieveStories(New))
            case cmd:FetchBestStories => processCommand[BestStoriesFetched](Best, cmd, newsService.retrieveStories(Best))
          }

        }
        def identifyStoryTypeFetchSize: (StoryType, Int) = incomingCommand match {
          case FetchTopStories(num, _) => (Top, num.value)
          case FetchNewStories(num, _) => (New, num.value)
          case FetchBestStories(num, _) => (Best, num.value)
        }

        currentState match {
          case EmptyState => handleIncomingCommand
          case PopulatedState(_) if incomingCommand == ShutDown => Effect.stop()
          case PopulatedState(data) =>
            val storyTypeFetchSize = identifyStoryTypeFetchSize
            logger info s"Checking if ${storyTypeFetchSize._2} ${storyTypeFetchSize._1} stories have to be fetched ..."
            logger info s"Number of pre-existing news items: ${data.get((storyTypeFetchSize._1, LocalDate.now())).map(_.size).getOrElse(0)}"
            // Handle the command only if the state doesn't already contain the required number of items for indicated type
            if(data.get((storyTypeFetchSize._1, LocalDate.now())).map(_.size).getOrElse(0)<storyTypeFetchSize._2) {
              handleIncomingCommand
            } else {
              logger info s"Discarding command $incomingCommand"
              Effect.none
            }
        }

      }
    }

    /**
     * Defines how a new state is created by applying the successfully persisted event to the current state
     * The event handler must only update the state and never perform side effects.
     *
     * @return A function that accepts current state and persisted event to apply to return a new state
     */
    def onEvent:(State, Event) => State = {
      (currentState, persistedEvent) => {
        logger.info(s"Handling event ${persistedEvent.getClass.getSimpleName}. Current state: $currentState")
        def identifyStoryType: StoryType = persistedEvent match {
          case _: TopStoriesFetched => Top
          case _: NewStoriesFetched => New
          case _: BestStoriesFetched => Best
        }
        currentState match {
          case EmptyState => PopulatedState().withLatestNews(identifyStoryType, persistedEvent.date, persistedEvent.stories)
          case state: PopulatedState =>  state.withLatestNews(identifyStoryType, persistedEvent.date, persistedEvent.stories)
        }
      }

    }

    EventSourcedBehavior[Command, Event, State](createPersistenceIdentifier, initialState,
      commandHandler = onCommand, eventHandler = onEvent)
  }
}
