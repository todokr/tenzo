package tenzo

import tenzo.datastore.JdbcConfig

import java.sql.DriverManager
import java.util.Properties
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
  val Sql =
    """
      |select
      |    tc.constraint_name,
      |    tc.table_schema,
      |    ccu.table_name as to_table,
      |    ccu.column_name as to_column,
      |    kcu.table_name as from_table,
      |    kcu.column_name as from_column
      |from information_schema.table_constraints as tc
      |inner join information_schema.constraint_column_usage as ccu on tc.constraint_name = ccu.constraint_name
      |inner join information_schema.key_column_usage as kcu on tc.constraint_name = kcu.constraint_name
      |where
      |    tc.constraint_type = 'FOREIGN KEY'
      |""".stripMargin


  def main(args: Array[String]): Unit = {

    val reader   = scala.io.Source.fromResource(propFile).bufferedReader()
    val p        = new Properties()
    p.load(reader)
    val conf = JdbcConfig.from(p)
    Class.forName(conf.driver)
    val conn = DriverManager.getConnection(conf.url, conf.user, conf.password)
    val st = conn.prepareStatement(Sql)
    val rs = st.executeQuery()
    val results = Iterator.continually(rs).takeWhile(_.next()).map { rs =>
      Reference(
        rs.getString("table_schema"),
        rs.getString("constraint_name"),
        rs.getString("to_table"),
        rs.getString("to_column"),
        rs.getString("from_table"),
        rs.getString("from_column")
      )
    }

    results.foreach(println)
  }
}



case class Reference(
  tableSchema: String,
  constraintName: String,
  fromTable: String,
  fromColumn: String,
  toTable: String,
  toColumn: String) {
  override def toString: String =
    s"""[$tableSchema] $toTable.$toColumn <-- $fromTable.$fromColumn ($constraintName)"""
}