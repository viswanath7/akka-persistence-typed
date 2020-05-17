package com.example.behaviour

import java.time.LocalDate

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import cats.data.NonEmptyList
import cats.effect.IO
import com.example.domain.StoryType.{Best, New, Top}
import com.example.domain._
import com.example.service.HackerNewsService._
import shapeless.LabelledGeneric

object MyPersistentBehaviour {

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
   * Defines how to handle an incoming command given the current state, by producing an Effect.
   * An effect is a directive that defines event(s) to persist if any
   *
   * @return A function that accepts current state and incoming command to return an Effect.
   */
  def onCommand: (State, Command) => Effect[Event, State] = {

    def processCommand[E <: Event](number:PositiveIntUpto20, fn: PositiveIntUpto20 => IO[List[HackerNewsStory]] )(implicit labelledGeneric: LabelledGeneric[E]): Effect[Event, State] = {
      def fetchNews: List[News] = fn(number).unsafeRunSync().map(hns => News(hns.title, hns.url))
      val news = fetchNews
      if(news.isEmpty) Effect.none
      else Effect.persist( (LocalDate.now(), NonEmptyList fromListUnsafe news).toCaseClass[E]).thenNoReply()
    }

    (currentState, incomingCommand) => {
      def handleIncomingCommand: Effect[Event, State] = {
        incomingCommand match {
          case FetchTopStories(numberOfItems) => processCommand[TopStoriesFetched](numberOfItems, topStories)
          case FetchNewStories(numberOfItems) => processCommand[NewStoriesFetched](numberOfItems, newStories)
          case FetchBestStories(numberOfItems) => processCommand[BestStoriesFetched](numberOfItems, bestStories)
        }
      }

      def identifyStoryTypeFetchSize: (StoryType, Int) = incomingCommand match {
        case FetchTopStories(num) => (Top, num.value)
        case FetchNewStories(num) => (New, num.value)
        case FetchBestStories(num) => (Best, num.value)
      }

      currentState match {
        case EmptyState => handleIncomingCommand
        case PopulatedState(data) =>
          val storyTypeFetchSize = identifyStoryTypeFetchSize
          // Handle the command only if the state doesn't already contain the required number of items for indicated type
          if(data.get( (storyTypeFetchSize._1, LocalDate.now()) ).exists(newsItems => newsItems.size < storyTypeFetchSize._2))
            handleIncomingCommand
          else Effect.none
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

      def identifyStoryType: StoryType = persistedEvent match {
        case _: TopStoriesFetched => Top
        case _: NewStoriesFetched => New
        case _: BestStoriesFetched => Best
      }
      //TODO: Reset state with reset command ?
      PopulatedState().withLatestNews(identifyStoryType, persistedEvent.date, persistedEvent.stories)
    }

  }

  /**
   * State when the entity is first created
   *
   * @return empty state
   */
  lazy val initialState: State = EmptyState

  def apply(): Behavior[Command] = {
    import akka.persistence.typed.scaladsl.EventSourcedBehavior
    EventSourcedBehavior[Command, Event, State](createPersistenceIdentifier, initialState,
      commandHandler = onCommand, eventHandler = onEvent)
  }
}
