package tenzo.dsl

private[dsl] trait Parsing {
  import fastparse._
  import NoWhitespace._

  def WSs[_: P]    = P(WS.rep)
  def Term[_: P]   = P(AlNum ~ (AlNum | WS | "->").rep)
  def AlNum[_: P]  = P((Number | Alpha).rep(1))
  def Number[_: P] = P(CharIn("0-9").rep(1))
  def Alpha[_: P]  = P(CharIn("A-z").rep(1))
  def WS[_: P]     = P(" ")
}
