package tenzo.datastore

import java.sql.DriverManager
import scala.util.Using

class MetadataLoader(conf: JdbcConfig) {
  Class.forName(conf.driver)

  def loadReferences(): Seq[Reference] =
    Using.resource(DriverManager.getConnection(conf.url, conf.user, conf.password)) { conn =>
      val stmt = conn.prepareStatement(ReferenceSql.stmt)
      val rs   = stmt.executeQuery()
      Iterator
        .continually(rs)
        .takeWhile(_.next())
        .map { rs =>
          Reference(
            rs.getString("table_schema"),
            rs.getString("constraint_name"),
            rs.getString("from_table"),
            rs.getString("from_column"),
            rs.getString("to_table"),
            rs.getString("to_column"),
          )
        }
        .toSeq
    }

  object ReferenceSql {
    val stmt: String =
      """select
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
  }

  def loadTableStructure(tables: Seq[String]): Seq[TableStructure] =
    Using.resource(DriverManager.getConnection(conf.url, conf.user, conf.password)) { conn =>
      val stmt = conn.prepareStatement(TableStructureSql.stmt(tables))
      tables.zipWithIndex.foreach { case (tbl, idx) =>
        stmt.setString(idx + 1, tbl)
      }
      val rs = stmt.executeQuery()
      val records = Iterator
        .continually(rs)
        .takeWhile(_.next())
        .map { rs =>
          (
            rs.getString("table_schema"),
            rs.getString("table_name"),
            rs.getString("column_name"),
            rs.getString("is_nullable"),
            rs.getString("data_type")
          )
        }
        .toSeq

      records
        .groupBy { case (schema, table, _, _, _) => (schema, table) }
        .map { case ((schema, table), cols) =>
          val columns = cols.map { case (_, _, name, isNull, tpe) =>
            val nullable = isNull == "YES"
            TableStructure.Column(name, tpe, nullable)
          }
          TableStructure(schema, table, columns)
        }
        .toSeq
    }

  object TableStructureSql {
    def stmt(tables: Seq[String]): String = {
      val placeholder = tables.map(_ => "?").mkString(",")
      s"""select
         |    c.table_schema,
         |    c.table_name,
         |    c.column_name,
         |    c.is_nullable,
         |    c.data_type
         |from information_schema.columns as c
         |where c.table_name in ($placeholder)
         |order by c.table_name, c.ordinal_position""".stripMargin
    }
  }
}
