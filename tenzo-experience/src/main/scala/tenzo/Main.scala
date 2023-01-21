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
           |# tableName2
           |#  tableName3
         """
    println("hello")
  }

  object ShitakuParser {
    import fastparse._, NoWhitespace._

    def tableName[_: P] = P("#" ~ " ".rep ~ CharPred(!_.isWhitespace).rep.! ~ (End|"\n"))
    def expr[_: P] = P(Start ~ tableName.rep)
    //def upperLine[_: P] = P("┌" | "─" | "┬" | "┐")
    //def delimiterLine[_: P] = P("├" | "─" | "┼" | "┤")
    //def lowerLine[_: P] = P("└" | "─" | "┴" |"┘")

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
