package models

import i18n.Messages

/**
 *
 * @author rodion
 */

abstract class HistoryAction {
  def localise(implicit m: Messages): String
}

case class Add() extends HistoryAction {
  def localise(implicit m: Messages) = m.activity.add
}

case class Modify() extends HistoryAction {
  def localise(implicit m: Messages) = m.activity.modify
}

case class Delete() extends HistoryAction {
  def localise(implicit m: Messages) = m.activity.delete
}
