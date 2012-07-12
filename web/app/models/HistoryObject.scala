package models

import i18n.Messages

/**
 *
 * @author rodion
 */

abstract class HistoryObject {
  def describe(implicit m: Messages): String
}
