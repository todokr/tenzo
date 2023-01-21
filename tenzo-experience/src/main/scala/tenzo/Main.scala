package tenzo

import scala.annotation.unused

object Main {
  implicit class ShitakuInterpolation(val sc: StringContext) extends AnyVal {

    def setup(@unused args: Any*): Seq[FocalTable] = {
      val input = sc.parts.map(_.stripMargin.linesIterator.filterNot(_.isBlank).mkString("\n")).mkString
      ShitakuParser.parseDsl(input)
    }
  }

  def main(args: Array[String]): Unit = {
    /*setup"""
      |# departments
      |┌───────┬───────────────────────────┐
      |│ alias │      department_name      │
      |├───────┼───────────────────────────┤
      |│ hr    │ Human Resource Department │
      |│ sales │ Sales Department          │
      |└───────┴───────────────────────────┘
      |
      |# users
      |┌──────────────┬───────────┬─────┬───────────────────┐
      |│    alias     │ user_name │ age │    department     │
      |├──────────────┼───────────┼─────┼───────────────────┤
      |│ hr_person    │ Jinnai    │  31 │ department(hr)    │
      |│ sales_parson │ Urita     │  27 │ department(sales) │
      |└──────────────┴───────────┴─────┴───────────────────┘
      |"""*/
    val result = setup"""
           |# departments
           |┌───────┬─────────────────────────┐
           |│ alias │    department_name      │
           |├───────┼─────────────────────────┤
           |│ hr    │ HumanResourceDepartment │
           |│ sales │ SalesDepartment         │
           |└───────┴─────────────────────────┘
           |
           |# users
           |┌──────────────┬───────────┬─────┬───────────────────┐
           |│    alias     │ user_name │ age │    department     │
           |├──────────────┼───────────┼─────┼───────────────────┤
           |│ hr_person    │ Jinnai    │  31 │ department(hr)    │
           |│ sales_parson │ Urita     │  27 │ department(sales) │
           |└──────────────┴───────────┴─────┴───────────────────┘
           |"""
    result.foreach { t =>
      println(s"${t.tableName} ---------------")
      t.rows.foreach(r => println(r.columns.map(c => s"${c.name}:${c.value}")))
    }
  }

  object ShitakuParser {
    import fastparse._, NoWhitespace._

    def tableName[_: P] = P("#" ~ WSs ~ NonEmptyStr.! ~ Newline)
    def header[_: P]    = P(("│" ~ WSs ~ NonEmptyStr.! ~ WSs).rep(1) ~ "│" ~ Newline)
    def content[_: P]   = P(("│" ~ WSs ~ NonEmptyStr.! ~ WSs).rep(1) ~ "│" ~ Newline)
    def focalTable[_: P] = P(tableName ~ upperLine ~ header ~ delimiterLine ~ content.rep ~ lowerLine).map {
      case (tableName, headers, contents) =>
        val rows = contents.map { values =>
          Row(headers.zip(values).map { case (header, value) => Column(header, SpecifiedValue(value))})
        }
        FocalTable(tableName, rows)
    }

    def expr[_: P] = P(focalTable.rep)

    def parseDsl(raw: String): Seq[FocalTable] =
      parse(raw, expr(_)).get.value

    def Number[_: P] = P(CharIn("0-9").rep(1))
    def Alpha[_: P] = P(CharIn("a-Z").rep(1))
    def NonEmptyStr[_: P]   = P(CharPred(!_.isWhitespace).rep(1))
    def upperLine[_: P]     = P("┌" ~ ("─" | "┬").rep ~ "┐" ~ Newline)
    def delimiterLine[_: P] = P("├" ~ ("─" | "┼").rep ~ "┤" ~ Newline)
    def lowerLine[_: P]     = P("└" ~ ("─" | "┴").rep ~ "┘" ~ (Newline | ""))
    def WSs[_: P]           = P(" ".rep)
    val Newline             = "\n"

  }
  case class FocalTable(
    tableName: String,
    rows: Seq[Row]
  )

  case class Row(columns: Seq[Column])

  case class Column(
    name: String,
    value: SpecifiedValue
  )

  trait SpecifiedValue
  object SpecifiedValue {
    case class Text(value: String) extends SpecifiedValue

    def apply(v: String): SpecifiedValue = Text(v)
  }
}
