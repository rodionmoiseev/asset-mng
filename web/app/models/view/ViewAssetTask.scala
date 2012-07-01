package models.view

/**
 *
 * @author rodion
 */

case class ViewAssetTask(id: Long,
                           user: String,
                           desc: String,
                           date: String,
                           tags: List[String])
