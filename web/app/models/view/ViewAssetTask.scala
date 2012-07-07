package models.view

/**
 *
 * @author rodion
 */

case class ViewAssetTask(id: Long,
                         asset_id: Long,
                         user: String,
                         description: String,
                         date: String,
                         tags: List[String],
                         icons: List[String])
