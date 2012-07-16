package models

import i18n.Messages

/**
 *
 * @author rodion
 */

abstract class HistoryObject {
  def id: Long

  def describe(implicit m: Messages): String
}
