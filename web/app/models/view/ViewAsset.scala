package models.view

/**
 *
 * @author rodion
 */

case class ViewAsset(id: Long,
                     hostname: String,
                     ip: String,
                     description: String,
                     admin: String,
                     parent_id: Option[Long])
