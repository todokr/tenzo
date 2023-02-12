package tenzo.datastore

import java.util.Properties

object ConfigLoader {
  def load(propFileName: String = DefaultPropFileName): JdbcConfig = {
    val reader = scala.io.Source.fromResource(propFileName).bufferedReader()
    val p      = new Properties()
    p.load(reader)
    JdbcConfig.from(p)
  }

  private val DefaultPropFileName = "shitaku.properties"
}

private[datastore] object JdbcConfig {
  def from(properties: Properties): JdbcConfig = {
    val driver   = properties.getString(DriverKey)
    val url      = properties.getString(UrlKey)
    val user     = properties.getString(UserKey)
    val password = properties.getString(PasswordKey)
    val schema =  properties.getStringOr(SchemaKey, DefaultSchema)
    JdbcConfig(driver, url, user, password, schema)
  }

  private val DriverKey   = "db.driver"
  private val UrlKey      = "db.url"
  private val UserKey     = "db.user"
  private val PasswordKey = "db.password"
  private val SchemaKey   = "db.schema"

  private val DefaultSchema = "public"

  private class JdbcConfigException(keyName: String) extends Exception(s"$keyName is not set")

  private implicit class RichProperties(props: Properties) {

    def getString(key: String): String =
      props.computeIfAbsent(key, _ => throw new JdbcConfigException(key)).toString

    def getStringOr(key: String, default: String): String =
      props.computeIfAbsent(key, _ => default).toString
  }
}

final case class JdbcConfig(driver: String, url: String, user: String, password: String, schema: String) {
  override def toString: String =
    s"""
       |driver=$driver
       |url=$url
       |user=$user
       |password=${password.map(_ => "*").mkString}
       |""".stripMargin
}