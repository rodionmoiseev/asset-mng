package controllers.data

import play.api.mvc._
import com.codahale.jerkson.Json._
import dao.Module._
import models._
import controllers.Application.AssetMngAction
import java.util.Date
import models.Undo
import models.Add
import models.HistoryEntry
import view.ViewHistoryEntry
import scala.Some
import models.Delete
import dao.DB

/**
 *
 * @author rodion
 */

object Activity extends Controller {
  def activity2view = (entry: HistoryEntry) =>
    ViewHistoryEntry(entry.id, entry.user, entry.dateStr, entry.action.localise, entry.obj.describe, canUndo(entry))

  private def canUndo(entry: HistoryEntry): Boolean = {
    entry.action match {
      // 'undo' cannot be undone (at least not for the moment)
      case Undo(_) => false
      // 'delete' can only be undo when the target item does not exist
      case Delete() => !findDB(entry.obj).all.exists(_.id == entry.obj.id)
      // 'add' and 'modify' actions can be undone as long as the item still exists
      case _ => findDB(entry.obj).all.exists(_.id == entry.obj.id)
    }
  }

  private def findDB(obj: HistoryObject) = obj match {
    case asset: Asset => assetsDB
    case assetTask: AssetTask => assetTasksDB
  }

  def list = AssetMngAction {
    (user, request) =>
      Ok(generate(activityDB.all.reverse map activity2view))
  }

  def undo(id: Long) = AssetMngAction {
    (user, request) =>
      val newHistoryEntry = activityDB.all.find( (entry) => entry.id == id && canUndo(entry) ) match {
        case Some(entry) => {
          entry.action match {
            case Add() => delete(entry, user)
            case Delete() => add(entry, user)
            case _ => None
          }
        }
        case _ => None
      }
      newHistoryEntry match {
        case Some(entry) => Ok(generate(Map(
          "status" -> "OK",
          "activity" -> activity2view(entry))))
        case None => BadRequest(generate(Map("status" -> "ERROR")))
      }
  }

  private def delete(entry: HistoryEntry, user: String): Option[HistoryEntry] = {
    entry.obj match {
      case obj: Asset => Some(Assets.deleteAsset(obj.id, user, Undo(entry.action)))
      case obj: AssetTask => Some(AssetTasks.deleteTask(obj.id, user, Undo(entry.action)))
      case _ => None
    }
  }

  private def add(entry: HistoryEntry, user: String): Option[HistoryEntry] = {
    entry.obj match {
      case obj: Asset => Some(Assets.addAsset(obj, user, Undo(entry.action)))
      case obj: AssetTask => Some(AssetTasks.addTask(obj, user, Undo(entry.action)))
      case _ => None
    }
  }
}
