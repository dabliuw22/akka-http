package com.leysoft

import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterEach, Matchers, Suite, WordSpec}

protected[leysoft] trait Specification extends WordSpec with Matchers with MockFactory with BeforeAndAfterEach with ScalaFutures {}