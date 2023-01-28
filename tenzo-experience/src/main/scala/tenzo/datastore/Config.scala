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
