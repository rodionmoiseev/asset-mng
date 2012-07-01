package models

import java.util.Date

/**
 *
 * @author rodion
 */

case class AssetTask(id: Long, user: String, desc: String, startDate: Date, tags: List[String]){
  def dateStr = "%1$tm/%1$td %1$tH:%1$tM".format(startDate)
}
