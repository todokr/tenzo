package tenzo.dsl

object DslParser extends Parsing {
  import fastparse.{parse => fParse, _}
  import NoWhitespace._
  import FocalTable._

  /** Normalize row DSL input to parse */
  def normalize(raw: String): Dsl =
    Dsl(raw.stripMargin.linesIterator.map(_.trim).filter(_.nonEmpty).mkString(Newline))

  /** parse DSL to data model */
  def parse(dsl: Dsl): Seq[FocalTable] = {
    fParse(dsl.value, DslExpr(_)) match {
      case Parsed.Success(value, _) => value
      case e: Parsed.Failure        => throw new Exception(e.toString())
    }
  }

  def DslExpr[_: P]: P[Seq[FocalTable]] = P(FocalTableExpr.rep)

  def FocalTableExpr[_: P]: P[FocalTable] =
    P(TableName ~ UpperLine ~ HeaderLine ~ DelimiterLine ~ ContentLines ~ LowerLine).map {
      case (tableName, headers, contents) =>
        val rows = contents.map { values =>
          val cols = headers.zip(values).map { case (header, value) => Column(header, SpecifiedValue(value)) }
          Row(cols)
        }
        FocalTable(tableName, rows)
    }

  def TableName[_: P]: P[String]              = P("#" ~ WSs ~ Term.! ~ Newline)
  def HeaderLine[_: P]: P[Seq[String]]        = P(("│" ~ WSs ~ Term.!).rep(1) ~ "│" ~ Newline).map(_.map(_.trim))
  def ContentLines[_: P]: P[Seq[Seq[String]]] = P(ContentLine.rep(1))
  def ContentLine[_: P]: P[Seq[String]]       = P(("│" ~ WSs ~ Term.!).rep(1) ~ "│" ~ Newline).map(_.map(_.trim))

  def UpperLine[_: P]: P[Unit]     = P("┌" ~ ("─" | "┬").rep ~ "┐" ~ Newline)
  def DelimiterLine[_: P]: P[Unit] = P("├" ~ ("─" | "┼").rep ~ "┤" ~ Newline)
  def LowerLine[_: P]: P[Unit]     = P("└" ~ ("─" | "┴").rep ~ "┘" ~ (Newline | ""))

  private val Newline = "\n"

  final case class Dsl(value: String) extends AnyVal {
    override def toString: String = value
  }
}
