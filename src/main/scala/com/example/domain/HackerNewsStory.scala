package com.example.domain

import eu.timepit.refined.api.Refined
import eu.timepit.refined.numeric.{NonNegative, Positive}
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.string.NonEmptyString

final case class HackerNewsStory(by:NonEmptyString, descendants: Int Refined NonNegative,
                                 id: Int, kids: List[Int], score: Int, time: Int Refined Positive,
                                 title:NonEmptyString, `type`: NonEmptyString, url: String Refined Url)