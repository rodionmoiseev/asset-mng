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
  def localise(implicit m: Messages) = m.activity.added
}

case class Modify() extends HistoryAction {
  def localise(implicit m: Messages) = m.activity.modified
}

case class Delete() extends HistoryAction {
  def localise(implicit m: Messages) = m.activity.deleted
}

case class Undo(action: HistoryAction) extends HistoryAction {
  def localise(implicit m: Messages): String = m.activity.undone(localiseAction(action))

  private def localiseAction(action: HistoryAction)(implicit m: Messages): String = action match {
    case Add() => m.activity.addition
    case Modify() => m.activity.modification
    case Delete() => m.activity.deletion
    case Undo(undoneAction) => m.activity.undo(localiseAction(undoneAction))
  }
}
