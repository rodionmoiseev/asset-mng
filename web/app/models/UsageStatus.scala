package models

/**
 *
 * @author rodion
 */

trait UsageStatus extends Product with Serializable {
  def name: String

  override def productElement(n: Int) = List(name)(n)
}

case class Used() extends UsageStatus {
  override def name = "Used"
}

case class Free() extends UsageStatus {
  override def name = "Free"
}
