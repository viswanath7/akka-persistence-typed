package com.example.service

import java.net.URI

import cats.Eval
import cats.effect.{ContextShift, IO}
import com.example.domain._
import com.example.domain.exception.HTTPClientException
import eu.timepit.refined.auto._
import io.circe._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.circe._
import sttp.client.{ResponseError, asStringAlways, basicRequest}
import sttp.model.Uri
import com.example.domain.StoryType._

import scala.language.implicitConversions

object HackerNewsService {

  private[this] val configuration = Eval.later {
    import com.typesafe.config.ConfigFactory
    ConfigFactory.load()
  }.memoize

  private[this] val baseURL = for {
    conf <- configuration
  } yield conf.getString("backend.service.baseURL")

  private def retrieveStories(storyType: StoryType, numberOfItems: PositiveIntUpto20): IO[List[HackerNewsStory]] = {

    implicit val contextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

    implicit def liftEither[T](input: IO[Either[ResponseError[Error], T]]): IO[T] =
      input.flatMap(responseErrorOrResult => {
        import cats.syntax.either._
        IO.fromEither(responseErrorOrResult leftMap (HTTPClientException(_)))
      })

    /**
     * Given an upper limit, retrieves a list of URLs containing identifiers of stories
     *
     * @return a list of URLs containing identifiers of stories
     */
    def fetchStoryIdentifiers: IO[List[String]] = {

      def fetchTextResponse(url: URI): IO[String] = AsyncHttpClientCatsBackend[IO]().flatMap { implicit backend =>
        basicRequest
          .header("Accept", "text/plain")
          .get(Uri(url))
          .response(asStringAlways)
          .send()
          .map(response => response.body)
          .guarantee(backend.close())
      }

      import io.circe.parser._
      for {
        serviceURL <- IO(createServiceURL(storyType))
        rawIdentifiers <- fetchTextResponse(new URI(serviceURL))
        json <- IO.fromEither(parse(rawIdentifiers))
        listOfIdentifiers <- IO.fromEither(json.as[LazyList[Int]])
      } yield listOfIdentifiers
        .map(listOfIdentifiers.take(numberOfItems))
        .map(id => s"${baseURL.value}/item/$id.json?print=pretty")
        .toList
    }

    /**
     * Given a list of URLs to fetch stories, makes HTTP calls and fetches them in parallel
     *
     * @param storyURLs URLs of stories to fetch
     * @return List of hack rank stories' details.
     */
    def fetchStories(storyURLs: List[String]): IO[List[HackerNewsStory]] = {

      def fetchJSONResponse[T <: Product](url: URI)(implicit evidence: Decoder[T]): IO[T] =
        AsyncHttpClientCatsBackend[IO]().flatMap { implicit backend =>
          basicRequest
            .header("Accept", "application/json")
            .get(Uri(url))
            .response(asJson[T])
            .send()
            .map(response => response.body)
            .guarantee(backend.close())
        }

      import monix.eval._
      import monix.reactive._
      import monix.execution.Scheduler.Implicits.global
      import io.circe.refined._
      import eu.timepit.refined.auto._
      import io.circe.generic.auto._
      Observable.fromIterable(storyURLs)
        .mapParallelOrdered(8) {
          storyURL => Task from fetchJSONResponse[HackerNewsStory](new URI(storyURL))
        }.toListL.to[IO]
    }

    def createServiceURL(storyType: StoryType): String = baseURL.map(_ + storyType.urlPath.value).value

    for {
      storyURLs <- fetchStoryIdentifiers
      stories <- fetchStories(storyURLs)
    } yield stories
  }

  def topStories(n: PositiveIntUpto20): IO[List[HackerNewsStory]] = retrieveStories(Top, n)
  def newStories(n: PositiveIntUpto20): IO[List[HackerNewsStory]] = retrieveStories(New, n)
  def bestStories(n: PositiveIntUpto20): IO[List[HackerNewsStory]] = retrieveStories(Best, n)

}
