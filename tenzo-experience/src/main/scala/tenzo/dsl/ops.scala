package tenzo.dsl

import scala.annotation.unused
import Parser.{normalize, parse}

object ops {
  import scala.util.chaining.scalaUtilChainingOps

  implicit class ShitakuInterpolation(val sc: StringContext) extends AnyVal {
    def setup(@unused args: Any*): Seq[FocalTable] = {
      val input = sc.parts.mkString
      input.pipe(normalize).pipe(parse)
    }
  }
}
