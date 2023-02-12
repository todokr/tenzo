package tenzo

import tenzo.datastore.{ConfigLoader, MetadataLoader, References, Tables}
object Main {
  import tenzo.dsl.ops._
  def main(args: Array[String]): Unit = {
    val result =
      setup"""
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

}

object JDBCTest {

  def main(args: Array[String]): Unit = {
    val conf   = ConfigLoader.load()
    val loader = new MetadataLoader(conf)

    val references = loader.loadReferences()
    val deps = references.dependencies("target_users")
    println(deps)

    val tables = loader.loadTables()

    println(tables.size)

    // println(tables.toString)
  }
}

class Shitaku private (tables: Tables, refs: References) {
  override def toString: String = {
    refs.toString + "\n" + tables.toString
  }
}

object Shitaku {

  def init(): Shitaku = {
    val conf = ConfigLoader.load()
    val loader = new MetadataLoader(conf)
    val tables = loader.loadTables()
    val references = loader.loadReferences()
    new Shitaku(tables, references)
  }
}