package models

import java.util

/**
 *
 * @author rodion
 */

case class HistoryEntry(id: Long, user: String, date: util.Date, action: HistoryAction, obj: HistoryObject) extends Persistent[HistoryEntry] {
  def dateStr = "%1$tm/%1$td %1$tH:%1$tM".format(date)

  def withId(id: Long) = copy(id)
}
