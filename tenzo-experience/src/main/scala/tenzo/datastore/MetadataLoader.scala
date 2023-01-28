package tenzo.datastore

import java.sql.DriverManager

object MetadataLoader {

  def load(conf: JdbcConfig): DatastoreMetadata = {
    Class.forName(conf.driver)
    val conn = DriverManager.getConnection(conf.url, conf.user, conf.password)
    val st   = conn.prepareStatement(ReferenceSql)
    val rs   = st.executeQuery()
    val references = Iterator
      .continually(rs)
      .takeWhile(_.next())
      .map { rs =>
        Reference(
          rs.getString("table_schema"),
          rs.getString("constraint_name"),
          rs.getString("to_table"),
          rs.getString("to_column"),
          rs.getString("from_table"),
          rs.getString("from_column")
        )
      }
      .toSeq
    DatastoreMetadata(references)
  }

  private val ReferenceSql =
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
