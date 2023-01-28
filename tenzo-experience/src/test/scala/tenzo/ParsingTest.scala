package tenzo

import fastparse.parseInputRaw
import org.scalatest.freespec.AnyFreeSpec
import tenzo.dsl.Main.{Parsing, ShitakuParser, SpecifiedValue}

class ParsingTest extends AnyFreeSpec {
  import fastparse.parse
  private val target = new Parsing {}

  "ShitakuParser" - {
    val target = ShitakuParser

    "normalize" in {
      val input =
        """
          |
          |  # departments
          |  ┌───────┬───────────────────────────┐
          |  │ alias │      department_name      │
          |  ├───────┼───────────────────────────┤
          |  │ hr    │ Human Resource Department │
          |  │ sales │ Sales Department          │
          |  └───────┴───────────────────────────┘
          |
          |  """
      val result = target.normalize(input)
      val expected =
        """# departments
          |┌───────┬───────────────────────────┐
          |│ alias │      department_name      │
          |├───────┼───────────────────────────┤
          |│ hr    │ Human Resource Department │
          |│ sales │ Sales Department          │
          |└───────┴───────────────────────────┘""".stripMargin

      assert(result.value === expected)
    }

    "parse" in {
      import SpecifiedValue.Text

      val input = target.Dsl(
        """# departments
          |┌───────┬───────────────────────────┐
          |│ alias │      department_name      │
          |├───────┼───────────────────────────┤
          |│ hr    │ Human Resource Department │
          |│ sales │ Sales Department          │
          |└───────┴───────────────────────────┘
          |# users
          |┌──────────────┬───────────┬─────┬───────────────────┐
          |│    alias     │ user_name │ age │  department_id    │
          |├──────────────┼───────────┼─────┼───────────────────┤
          |│ hr_person    │ Jinnai    │  31 │ department(hr)    │
          |│ sales_parson │ Urita     │  27 │ department(sales) │
          |└──────────────┴───────────┴─────┴───────────────────┘""".stripMargin
      )
      val Seq(actual1, actual2) = target.parse(input)

      // departments
      assert(actual1.tableName === "departments")

      val Seq(row1_1, row1_2) = actual1.rows
      val Seq(col1_1_1, col1_1_2) = row1_1.columns
      assert(col1_1_1.name === "alias")
      assert(col1_1_1.value === Text("hr"))
      assert(col1_1_2.name === "department_name")
      assert(col1_1_2.value === Text("Human Resource Department"))

      val Seq(col1_2_1, col1_2_2) = row1_2.columns
      assert(col1_2_1.name === "alias")
      assert(col1_2_1.value === Text("sales"))
      assert(col1_2_2.name === "department_name")
      assert(col1_2_2.value === Text("Sales Department"))

      // users
      assert(actual2.tableName === "users")

      val Seq(row2_1, row2_2) = actual2.rows
      val Seq(col2_1_1, col2_1_2, col2_1_3, col2_1_4) = row2_1.columns
      assert(col2_1_1.name === "alias")
      assert(col2_1_1.value === Text("hr_person"))
      assert(col2_1_2.name === "user_name")
      assert(col2_1_2.value === Text("Jinnai"))
      assert(col2_1_3.name === "age")
      assert(col2_1_3.value === Text("31"))
      assert(col2_1_4.name === "department")
      assert(col2_1_4.value === Text("department(hr)"))

      val Seq(col2_2_1, col2_2_2, col2_2_3, col2_2_4) = row2_2.columns
      assert(col2_2_1.name === "alias")
      assert(col2_2_1.value === Text("sales_parson"))
      assert(col2_2_2.name === "user_name")
      assert(col2_2_2.value === Text("Urita"))
      assert(col2_2_3.name === "age")
      assert(col2_2_3.value === Text("27"))
      assert(col2_2_4.name === "department")
      assert(col2_2_4.value === Text("department(sales)"))
    }

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
        """ # the table name
          |""".stripMargin
      val result = parse(input, target.TableName(_))
      assert(result.get.value === "the table name")
    }

    "HeaderLine" in {
      val input =
        """│    alias     │ user_name │ age │    department     │
          |""".stripMargin
      val result = parse(input, target.HeaderLine(_))
      assert(result.get.value === Seq("alias", "user_name", "age", "department"))
    }

    "ContentLine" in {
      val input =
        """│  hr_person     │ Taro Yamada   │ 31 │    Human Resource     │
          |""".stripMargin
      val result = parse(input, target.ContentLine(_))
      assert(result.get.value === Seq("hr_person", "Taro Yamada", "31", "Human Resource"))
    }

    "ContentLines" in {
      val input =
        """│ hr_person    │ Jinnai    │  31 │ department(hr)    │
          |│ sales_parson │ Urita     │  27 │ department(sales) │
          |""".stripMargin
      val result = parse(input, target.ContentLines(_))
      assert(result.get.value ===
        Seq(
          Seq("hr_person", "Jinnai", "31", "department(hr)"),
          Seq("sales_parson", "Urita", "27", "department(sales)")
      ))
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
