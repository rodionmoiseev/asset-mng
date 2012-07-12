package models.view

import models.HistoryAction

/**
 *
 * @author rodion
 */

case class ViewHistoryEntry(id: Long, user: String, date: String, action: String, obj: String)