package com.leysoft

import org.scalamock.scalatest.MockFactory
import org.scalatest.{Matchers, WordSpec}

protected[leysoft] trait Specification extends WordSpec with Matchers with MockFactory {}