package tenzo.datastore

class References(refs: Seq[Reference]) {
  def dependencies(from: String): Seq[String] = {
    val fromRef = refs.find(_.fromTable == from)
    val linear =
      Iterator
        .iterate(fromRef)(_.flatMap(r => refs.find(_.fromTable == r.toTable)))
        .takeWhile(x => x.nonEmpty && x.exists(r => r.fromTable != r.toTable))
        .collect { case Some(x) => x }
        .toSeq
    linear.head.fromTable +: linear.map(_.toTable)
  }
}

final case class Reference(
  tableSchema: String,
  constraintName: String,
  fromTable: String,
  fromColumn: String,
  toTable: String,
  toColumn: String
) {
  override def toString: String =
    s"""[$tableSchema] $toTable.$toColumn <-- $fromTable.$fromColumn ($constraintName)"""
}

class Tables(tbls: Seq[Table]) {
  def size: Int = tbls.size

  override def toString: String =
    tbls.map { t =>
      val cols = t.columns.map { col =>
        s"""${if (col.isPk) "[PK] " else ""}${col.name} ${col.dataType} ${if (col.nullable) "null" else "not null"}"""
      }.mkString("\n")
      s"""${"=" * 30}
         |${t.tableSchema}.${t.tableName}
         |${"=" * 30}
         |$cols
         |""".stripMargin
    }.mkString("\n")
}

final case class Table(
  tableSchema: String,
  tableName: String,
  columns: Seq[Table.Column]
)

object Table {
  final case class Column(
    name: String,
    dataType: String,
    isPk: Boolean,
    nullable: Boolean
  )
}
