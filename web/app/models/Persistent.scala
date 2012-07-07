package models

/**
 *
 * @author rodion
 */

trait Persistent[A] {
  def id: Long
  def withId(id: Long): A
}
