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
                     tags: List[String],
                     parent_id: Option[Long],
                     usageStatus: String,
                     status: ViewAssetStatus)
