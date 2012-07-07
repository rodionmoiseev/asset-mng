package models

/**
 *
 * @author rodion
 */

case class Asset(
                  id: Long,
                  hostname: String,
                  ip: String,
                  description: String,
                  admin: String,
                  parent_id: Option[Long]) extends Persistent[Asset] {
  def withId(id: Long) = copy(id)
}
