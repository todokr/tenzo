package tenzo.datastore

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

final case class TableStructure(
  tableSchema: String,
  tableName: String,
  columns: Seq[TableStructure.Column]
)

object TableStructure {
  final case class Column(
    name: String,
    dataType: String,
    isPk: Boolean,
    nullable: Boolean
  )
}
