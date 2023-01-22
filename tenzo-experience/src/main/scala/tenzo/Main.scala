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
           |"""
    result.foreach { t =>
      println(s"""${t.tableName}
                 |-----------------------------""".stripMargin)
      t.rows.foreach(row => println(row.columns.map(c => s"${c.name}:${c.value}").mkString(", ")))
      println("--------------------------")
      println()
    }
  }

  trait Parsing {
    import fastparse._, NoWhitespace._

    def UpperLine[_: P]     = P("┌" ~ ("─" | "┬").rep ~ "┐" ~ Newline)
    def DelimiterLine[_: P] = P("├" ~ ("─" | "┼").rep ~ "┤" ~ Newline)
    def LowerLine[_: P]     = P("└" ~ ("─" | "┴").rep ~ "┘" ~ (Newline | ""))
    def WSs[_: P]           = P(" ".rep)
    def AlNum[_: P]         = P((Number | Alpha).rep(1))
    def Number[_: P]        = P(CharIn("0-9").rep(1))
    def Alpha[_: P]         = P(CharIn("A-z").rep(1))

    val Newline = "\n"
  }

  object ShitakuParser extends Parsing {
    import fastparse._, NoWhitespace._

    def tableName[_: P] = P("#" ~ WSs ~ AlNum.rep.! ~ Newline)
    def header[_: P]    = P(("│" ~ WSs ~ AlNum.rep.! ~ WSs).rep(1) ~ "│" ~ Newline)
    def content[_: P]   = P(("│" ~ WSs ~ (AlNum | " ").rep.! ~ WSs).rep(1) ~ "│" ~ Newline)
    def focalTable[_: P] = P(tableName ~ UpperLine ~ header ~ DelimiterLine ~ content.rep ~ LowerLine).map {
      case (tableName, headers, contents) =>
        val rows = contents.map { values =>
          Row(headers.zip(values).map { case (header, value) => Column(header, SpecifiedValue(value)) })
        }
        FocalTable(tableName, rows)
    }

    def expr[_: P] = P(focalTable.rep)

    def parseDsl(raw: String): Seq[FocalTable] =
      parse(raw, expr(_)).get.value

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
    case class Text(value: String) extends SpecifiedValue {
      override def toString: String = value
    }

    def apply(v: String): SpecifiedValue = Text(v)
  }
}
