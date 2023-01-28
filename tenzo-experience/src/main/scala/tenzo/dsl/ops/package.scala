package tenzo.dsl

import scala.annotation.unused

package object ops {
  implicit class ShitakuInterpolation(val sc: StringContext) extends AnyVal {

    import Parser.{normalize, parse}

    import scala.util.chaining.scalaUtilChainingOps

    def setup(@unused args: Any*): Seq[FocalTable] = {
      val input = sc.parts.mkString
      input.pipe(normalize).pipe(parse)
    }
  }
}
