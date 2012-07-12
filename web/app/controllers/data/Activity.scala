package controllers.data

import play.api.mvc._
import com.codahale.jerkson.Json._
import dao.Module._
import models.HistoryEntry
import models.view.ViewHistoryEntry

/**
 *
 * @author rodion
 */

object Activity extends Controller {
  def activity2view = (entry: HistoryEntry) =>
    ViewHistoryEntry(entry.id, entry.user, entry.dateStr, entry.action.localise, entry.obj.describe)

  def list = Action {
    Ok(generate(activityDB.all.reverse map activity2view))
  }
}
