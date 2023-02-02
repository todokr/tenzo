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
    references.foreach(println)

    val from = references.find(_.fromTable == "target_users")
    val linear = Iterator
      .iterate(from)(_.flatMap(r => references.find(_.fromTable == r.toTable)))
      .takeWhile(x => x.nonEmpty && x.exists(r => r.fromTable != r.toTable))
      .collect { case Some(x) => x }
      .toSeq
    val res = linear.head.fromTable +: linear.map(_.toTable)
    println(res)

    val tables     = references.flatMap(r => Seq(r.toTable, r.fromTable)).distinct
    val structures = loader.loadTableStructure(tables)
    structures.foreach { structure =>
      println("=" * 30)
      println(s"${structure.tableSchema}.${structure.tableName}")
      println("=" * 30)
      structure.columns.foreach { col =>
        println(s"""${col.name} ${col.dataType} ${if (col.nullable) "null" else "not null"}""")
      }
      println()
    }
  }
}
