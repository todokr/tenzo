package tenzo
object Main {
  implicit class ShitakuInterpolation(val sc: StringContext) extends AnyVal {
    def setup(args: Any*): Seq[FocalTable] =
      ShitakuParser.parseDsl(sc.parts.map(_.stripMargin.trim).mkString)
  }

  def main(args: Array[String]): Unit = {
//    setup"""
//      |# departments
//      |┌───────┬───────────────────────────┐
//      |│ alias │      department_name      │
//      |├───────┼───────────────────────────┤
//      |│ hr    │ Human Resource Department │
//      |│ sales │ Sales Department          │
//      |└───────┴───────────────────────────┘
//      |
//      |# users
//      |┌──────────────┬───────────┬─────┬───────────────────┐
//      |│    alias     │ user_name │ age │    department     │
//      |├──────────────┼───────────┼─────┼───────────────────┤
//      |│ hr_person    │ Jinnai    │  31 │ department(hr)    │
//      |│ sales_parson │ Urita     │  27 │ department(sales) │
//      |└──────────────┴───────────┴─────┴───────────────────┘
//      |"""
    setup"""#tableName1
           |┌──────────────┬───────────┬─────┬───────────────────┐
           |│    alias     │ user_name │ age │    department     │
           |└──────────────┴───────────┴─────┴───────────────────┘
           |# tableName2
           |┌───────┬───────────────────────────┐
           |│ alias │      department_name      │
           |└───────┴───────────────────────────┘
           |# tableName3
           |┌───────┬───────────────────────────┐
           |│ alias │      department_name      │
           |└───────┴───────────────────────────┘
           |# tableName4
           |┌───────┬───────────────────────────┐
           |│ alias │      department_name      │
           |└───────┴───────────────────────────┘
           |"""
    println("hello")
  }

  object ShitakuParser {
    import fastparse._, NoWhitespace._



    def tableName[_: P] = P("#" ~ WSs ~ NonEmptyStr.! ~ "\n")
    def header[_: P]: P[Seq[String]] = P(("│" ~ WSs ~ NonEmptyStr.! ~ WSs).rep(1) ~ "│" ~ "\n")

    def expr[_: P] = P((tableName ~ upperLine ~ header ~ lowerLine).rep)

    def WSs[_: P] = P(" ".rep)

    def NonEmptyStr[_: P] = P(CharPred(!_.isWhitespace).rep(1))
    def upperLine[_: P] = P("┌" ~ ("─" | "┬").rep ~ "┐" ~ "\n")
    // def delimiterLine[_: P] = P("├" | "─" | "┼" | "┤")
    def lowerLine[_: P] = P("└" ~ ("─" | "┴").rep ~ "┘" ~ ("\n" | ""))

    def parseDsl(raw: String): Seq[FocalTable] = {
      println(parse(raw, expr(_)).get)
      Seq.empty
    }

  }
  case class FocalTable(
    tableName: String,
    columns: Seq[Column]
  )

  case class Column(name: String, value: SpecifiedValue)

  trait SpecifiedValue
}
