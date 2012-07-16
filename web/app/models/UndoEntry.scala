package models

import i18n.Messages

/**
 *
 * @author rodion
 */

case class UndoEntry(entryType: HistoryEntryType) extends HistoryObject {
  //undo-entry does not have a database associated and therefore does
  //not require an ID
  def id = -1L
  def describe(implicit m: Messages) = entryType.localise
}

trait HistoryEntryType {
  def localise(implicit m: Messages): String
}

case class AssetEntry() extends HistoryEntryType {
  def localise(implicit m: Messages) = m.activity.assetEntry
}

case class AssetTaskEntry() extends HistoryEntryType {
  def localise(implicit m: Messages)  = m.activity.assetTaskEntry
}
