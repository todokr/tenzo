package tenzo.datastore

final case class DatastoreMetadata(
  references: Seq[Reference]
)

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
