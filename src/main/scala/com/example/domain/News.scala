package com.example.domain

import eu.timepit.refined.api.Refined
import eu.timepit.refined.string.Url
import eu.timepit.refined.types.string.NonEmptyString

final case class News (title:NonEmptyString, url: String Refined Url)
