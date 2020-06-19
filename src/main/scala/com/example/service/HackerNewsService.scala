package com.example.service

import java.net.URI

import cats.Eval
import cats.effect.{ContextShift, IO}
import com.example.domain._
import com.example.domain.exception.HTTPClientException
import io.circe._
import sttp.client.asynchttpclient.cats.AsyncHttpClientCatsBackend
import sttp.client.circe._
import sttp.client.{ResponseError, asStringAlways, basicRequest}
import sttp.model.Uri
import com.typesafe.scalalogging.LazyLogging

import scala.language.implicitConversions

trait NewsService {
  def retrieveStories(storyType: StoryType)(numberOfItems: PositiveIntUpto20): IO[List[HackerNewsStory]]
}

object HackerNewsService extends NewsService with LazyLogging {

  private[this] val configuration = Eval.later {
    import com.typesafe.config.ConfigFactory
    ConfigFactory.load()
  }.memoize

  private[this] val baseURL = for {
    conf <- configuration
  } yield conf.getString("backend.service.baseURL")

  def retrieveStories(storyType: StoryType)(numberOfItems: PositiveIntUpto20): IO[List[HackerNewsStory]] = {
    logger info s"Fetching ${numberOfItems.value} '$storyType' stories from hacker news ..."
    implicit val contextShift: ContextShift[IO] = IO.contextShift(scala.concurrent.ExecutionContext.global)

    implicit def liftEither[T](input: IO[Either[ResponseError[Error], T]]): IO[T] =
      input.flatMap(responseErrorOrResult => {
        if (responseErrorOrResult.isLeft)
          logger error (s"Failed to fetch story!", responseErrorOrResult.swap.getOrElse(new RuntimeException("Failed to fetch story!")))
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
      val listOfIdentifiers: IO[List[Int]] = for {
        serviceURL <- IO(createServiceURL(storyType))
        rawIdentifiers <- fetchTextResponse(new URI(serviceURL))
        json <- IO.fromEither(parse(rawIdentifiers))
        listOfIdentifiers <- IO.fromEither(json.as[List[Int]])
      } yield listOfIdentifiers
      listOfIdentifiers.map(list => list.take(numberOfItems.value).map(id => s"${baseURL.value}/item/$id.json?print=pretty"))
    }

    /**
     * Given a list of URLs to fetch stories, makes HTTP calls and fetches them in parallel
     *
     * @param storyURLs URLs of stories to fetch
     * @return List of hack rank stories' details.
     */
    def fetchStories(storyURLs: List[String]): IO[List[HackerNewsStory]] = {
      logger debug s"Fetching stories from URLs ${storyURLs.mkString("\n", "\n", "\n")}"
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
      Observable.fromIterable(storyURLs)
        .mapParallelOrdered(8) {
          storyURL => {
            Task from fetchJSONResponse[HackerNewsStory](new URI(storyURL))
          }
        }.toListL.to[IO]
    }

    def createServiceURL(storyType: StoryType): String = baseURL.map(_ + storyType.urlPath.value).value

    for {
      storyURLs <- fetchStoryIdentifiers
      stories <- fetchStories(storyURLs)
    } yield stories
  }

}
