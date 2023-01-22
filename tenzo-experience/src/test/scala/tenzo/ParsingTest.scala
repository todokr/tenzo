package tenzo

import fastparse.P
import org.scalatest.freespec.AnyFreeSpec
import tenzo.Main.{Parsing, ShitakuParser}

class ParsingTest extends AnyFreeSpec {
  import fastparse.parse, fastparse.NoWhitespace._
  private val target = new Parsing {}

  "ShitakuParser" - {
    val target = ShitakuParser

    "UpperLine" in {
      val input =
        """┌──────────────┬───────────┬─────┬───────────────────┐
          |""".stripMargin
      val result = parse(input, target.UpperLine(_))
      assert(result.get.index === input.sizeIs)
    }

    "DelimiterLine" in {
      val input =
        """├──────────────┼───────────┼─────┼───────────────────┤
          |""".stripMargin
      val result = parse(input, target.DelimiterLine(_))
      assert(result.get.index === input.sizeIs)
    }

    "LowerLine" in {
      val input =
        """└──────────────┴───────────┴─────┴───────────────────┘""".stripMargin
      val result = parse(input, target.LowerLine(_))
      assert(result.get.index === input.sizeIs)
    }

    "TableName" in {
      val input =
        """# the table name
          |
          |""".stripMargin
      val result = parse(input, target.TableName(_))
      assert(result.get.value === "the table name")
    }
  }

  "Parsing" - {
    "WSs" in {
      val input = "   "
      val result = parse(input, target.WSs(_))
      assert(result.get.index === input.sizeIs)
    }

    "Term" in {
      val input = "Quick Brown Fox 2"
      val result = parse(input, target.Term(_))

      assert(result.get.index === input.sizeIs)
    }

    "Alnum" in {
      val input = "abc123"
      val result = parse(input, target.AlNum(_))
      assert(result.get.index === input.sizeIs)
    }
  }
}
