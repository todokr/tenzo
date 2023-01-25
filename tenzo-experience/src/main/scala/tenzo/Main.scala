package tenzo

import scala.annotation.unused

object Main {
  implicit class ShitakuInterpolation(val sc: StringContext) extends AnyVal {

    def setup(@unused args: Any*): Seq[FocalTable] = {
      val input = sc.parts.map(_.stripMargin.linesIterator.map(_.trim).filterNot(_.isBlank).mkString("\n")).mkString
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
    val result =
      setup""" # departments
           |  ┌───────┬───────────────────────────┐
           |  │ alias │      department_name      │
           |  └───────┴───────────────────────────┘
           |
           |# users
           |┌──────────────┬───────────┬─────┬───────────────────┐
           |│    alias     │ user_name │ age │    department     │
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


    def WSs[_: P]    = P(" ".rep)
    def Term[_: P]   = P(AlNum ~ (AlNum | " " | "(" | ")").rep)
    def AlNum[_: P]  = P((Number | Alpha).rep(1))
    def Number[_: P] = P(CharIn("0-9").rep(1))
    def Alpha[_: P]  = P(CharIn("A-z").rep(1))

    val Newline = "\n"
  }

  object ShitakuParser extends Parsing {
    import fastparse._, NoWhitespace._

    def parseDsl(raw: String): Seq[FocalTable] = {
      println(raw)
      parse(raw, DslExpr(_)) match {
        case Parsed.Success(value, _) => value
        case e: Parsed.Failure        => throw new Exception(e.toString())
      }
    }

    def DslExpr[_: P]: P[Seq[FocalTable]] = P(FocalTableExpr.rep)

    def FocalTableExpr[_: P] = P(TableName ~ UpperLine ~ HeaderLine ~ DelimiterLine  ~ LowerLine).map {
      case (tableName, headers) =>
//        val rows = contents.map { values =>
//          val cols = headers.zip(values).map { case (header, value) => Column(header, SpecifiedValue(value)) }
//          Row(cols)
//        }
        val cols = headers.map(h => Column(h, SpecifiedValue("")))
        FocalTable(tableName, Seq(Row(cols)))
    }

    def TableName[_: P]: P[String]        = P(WSs ~ "#" ~ WSs ~ Term.! ~ Newline)
    def HeaderLine[_: P]: P[Seq[String]]  = P(("│" ~ WSs ~ Term.!).rep(1) ~ "│" ~ Newline).map(_.map(_.trim))
    def ContentLines[_: P]: P[Seq[Seq[String]]] = P(ContentLine.rep(1))
    def ContentLine[_: P]: P[Seq[String]] = P(("│" ~ WSs ~ Term.!).rep(1) ~ "│" ~ Newline).map(_.map(_.trim))

    def UpperLine[_: P]: P[Unit]     = P("┌" ~ ("─" | "┬").rep ~ "┐" ~ Newline)
    def DelimiterLine[_: P]: P[Unit] = P("├" ~ ("─" | "┼").rep ~ "┤" ~ Newline)
    def LowerLine[_: P]: P[Unit]     = P("└" ~ ("─" | "┴").rep ~ "┘" ~ (Newline | ""))

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
