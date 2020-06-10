package com.example.domain

sealed trait Message
case object FetchNews extends Message
case object Terminate extends Message