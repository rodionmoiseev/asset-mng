package models

import java.util.Date

/**
 *
 * @author rodion
 */

case class AssetTask(id: Long,
                     asset_id: Long,
                     user: String,
                     description: String,
                     date: Date,
                     tags: List[String],
                     icons: List[String]) extends Persistent[AssetTask] {
  def withId(id: Long) = copy(id)
  def dateStr = "%1$tm/%1$td %1$tH:%1$tM".format(date)
}
