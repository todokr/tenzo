package tenzo

import org.scalatest.freespec.AnyFreeSpec
import tenzo.Main.Parsing

class ParsingTest extends AnyFreeSpec {
  import fastparse._
  private val target = new Parsing {}

  "Parsing" - {
    "UpperLine" in {
      val input =
        """┌──────────────┬───────────┬─────┬───────────────────┐
          |""".stripMargin
      val result = parse(input, target.UpperLine(_))
      assert(result.isSuccess)
    }

    "DelimiterLine" in {
      val input =
        """├──────────────┼───────────┼─────┼───────────────────┤
          |""".stripMargin
      val result = parse(input, target.DelimiterLine(_))
      assert(result.isSuccess)
    }

    "LowerLine" in {
      val input =
        """└──────────────┴───────────┴─────┴───────────────────┘""".stripMargin
      val result = parse(input, target.LowerLine(_))
      assert(result.isSuccess)
    }

    "WSs" in {
      val input = "   "
      val result = parse(input, target.WSs(_))
      assert(result.isSuccess)
    }

    "Alnum" in {
      val input = "abc123"
      val result = parse(input, target.AlNum(_))
      assert(result.isSuccess)
    }
  }
}
