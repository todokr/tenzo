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
    val driver   = properties.valueOf(DriverKey)
    val url      = properties.valueOf(UrlKey)
    val user     = properties.valueOf(UserKey)
    val password = properties.valueOf(PasswordKey)
    JdbcConfig(driver, url, user, password)
  }

  private val DriverKey   = "db.driver"
  private val UrlKey      = "db.url"
  private val UserKey     = "db.user"
  private val PasswordKey = "db.password"

  private class JdbcConfigException(keyName: String) extends Exception(s"$keyName is not set")

  private implicit class RichProperties(props: Properties) {
    def valueOf(key: String): String = Option(props.get(key))
      .map(_.toString)
      .getOrElse(throw new JdbcConfigException(key))
  }
}

final case class JdbcConfig(driver: String, url: String, user: String, password: String) {
  override def toString: String =
    s"""
       |driver=$driver
       |url=$url
       |user=$user
       |password=${password.map(_ => "*").mkString}
       |""".stripMargin
}