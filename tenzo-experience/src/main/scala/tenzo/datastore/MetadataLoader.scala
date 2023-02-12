package tenzo.datastore

import java.sql.DriverManager
import scala.util.Using

class MetadataLoader(conf: JdbcConfig) {
  import MetadataLoader._
  Class.forName(conf.driver)

  def loadReferences(): References =
    Using.resource(DriverManager.getConnection(conf.url, conf.user, conf.password)) { conn =>
      val stmt = conn.prepareStatement(ReferenceSql.stmt)
      val rs   = stmt.executeQuery()
      val refs =
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
              rs.getString("to_column")
            )
          }
          .toSeq
      new References(refs)
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

  def loadTables(): Tables =
    Using.resource(DriverManager.getConnection(conf.url, conf.user, conf.password)) { conn =>
      val stmt = conn.prepareStatement(TableStructureSql.stmt)
      val paramCount = TableStructureSql.stmt.count(_ == '?')
      (1 to paramCount).foreach(i => stmt.setString(i, conf.schema))
      val rs = stmt.executeQuery()
      val records = Iterator
        .continually(rs)
        .takeWhile(_.next())
        .map { rs =>
          (
            rs.getString("table_schema"),
            rs.getString("table_name"),
            rs.getString("column_name"),
            rs.getString("data_type"),
            rs.getBoolean("is_primary_key"),
            rs.getBoolean("is_nullable")
          )
        }
        .toSeq

      val tables =
        records
          .groupBy { case (schema, table, _, _, _, _) => (schema, table) }
          .map { case ((schema, table), cols) =>
            val columns = cols.map { case (_, _, name, tpe, isPk, nullable) =>
              Table.Column(name, tpe, isPk, nullable)
            }
            Table(schema, table, columns)
          }
          .toSeq

      new Tables(tables)
    }

  object TableStructureSql {
    val stmt: String = {
      s"""select c.table_schema,
         |       c.table_name,
         |       c.column_name,
         |       c.data_type,
         |       pk is not null as is_primary_key,
         |       c.is_nullable = 'YES' as is_nullable
         |from information_schema.columns as c
         |         left join (select cc.table_schema,
         |                           cc.table_name,
         |                           cc.column_name,
         |                           cc.constraint_name,
         |                           tc.constraint_type
         |                    from information_schema.constraint_column_usage as cc
         |                             inner join information_schema.table_constraints as tc
         |                                        on tc.constraint_type = 'PRIMARY KEY' and
         |                                           tc.table_schema = ? and
         |                                           cc.table_name = tc.table_name and
         |                                           cc.constraint_name = tc.constraint_name) as pk
         |                   on c.table_schema = pk.table_schema and
         |                      c.table_name = pk.table_name and
         |                      c.column_name = pk.column_name
         |where c.table_schema = ?
         |order by c.table_name, c.ordinal_position""".stripMargin
    }
  }
}

object MetadataLoader {
  private val SystemSchema = "pg_catalog"
}
