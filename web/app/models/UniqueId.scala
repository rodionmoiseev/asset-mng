package models

/**
 *
 * @author rodion
 */

case class UniqueId(id: Long) extends Persistent[UniqueId] {
  def withId(id: Long) = copy(id)
}
