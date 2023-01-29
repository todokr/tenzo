package tenzo

import tenzo.datastore.{ConfigLoader, MetadataLoader}
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
      println(
        s"""${t.tableName}
           |-----------------------------""".stripMargin)
      t.rows.foreach(row => println(row.columns.map(c => s"${c.name}:${c.value}").mkString(", ")))
      println("--------------------------")
      println()
    }
  }

}

object JDBCTest {

  def main(args: Array[String]): Unit = {
    val conf = ConfigLoader.load()
    val loader = new MetadataLoader(conf)

    val references = loader.loadReferences()
    references.foreach(println)

    val tables = references.flatMap(r => Seq(r.toTable, r.fromTable)).distinct
    val structures = loader.loadTableStructure(tables)
    structures.foreach { structure =>
      println("=" * 100)
      println(s"${structure.tableSchema}.${structure.tableName}")
      structure.columns.foreach { col =>
        println(s"${col.name} ${col.dataType} nullable:${col.nullable}")
      }
      println()
    }
  }
}