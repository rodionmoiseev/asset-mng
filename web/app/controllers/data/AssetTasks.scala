package controllers.data

import play.api.mvc._
import com.codahale.jerkson.Json._
import java.util.Date
import play.api.libs.json.Json._
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.view._
import models._
import i18n.Messages
import dao.Module._
import controllers.Application.AssetMngAction

/**
 *
 * @author rodion
 */

object AssetTasks extends Controller {
  implicit def m: Messages = Messages.m

  case class AssetTaskForm(asset_id: Long, description: String, user: String, tags: String, icons: String)

  val taskForm = Form(mapping(
    "asset_id" -> longNumber,
    "description" -> nonEmptyText,
    "user" -> nonEmptyText,
    "tags" -> text,
    "icons" -> text)
    (AssetTaskForm.apply)(AssetTaskForm.unapply))

  def task2view = (task: models.AssetTask) =>
    ViewAssetTask(task.id, task.asset_id, task.user, task.description, task.dateStr, task.tags, task.icons)

  def form2task = (taskForm: AssetTaskForm) =>
    AssetTask(-1L, taskForm.asset_id,
      taskForm.user,
      taskForm.description,
      new Date,
      toDelimitedList(taskForm.tags),
      toDelimitedList(taskForm.icons))

  def toDelimitedList(tags: String): List[String] = {
    if (!tags.trim.isEmpty)
      tags.split(",").map {
        _.trim
      }.collect {
        case s if !s.isEmpty => s
      }.toList
    else
      List()
  }

  def asTaskGroup = (entry: (Asset, List[AssetTask])) =>
    ViewAssetTaskGroup(Assets.asset2view(entry._1), entry._2 map task2view)

  def groupedByAsset = AssetMngAction {
    (user, request) =>
      val tasks = assetTasksDB.all
      val groups = assetsDB.all map {
        (asset) => (asset, tasks.filter((task) => task.asset_id == asset.id))
      }
      Ok(generate(groups map asTaskGroup))
  }

  def add = AssetMngAction {
    (user, request) =>
      request.body.asJson match {
        case Some(json) => taskForm.bind(json).fold(
          errors => BadRequest(errors.errorsAsJson),
          viewTask => {
            val newTask = assetTasksDB.save(form2task(viewTask))
            activityDB.save(new HistoryEntry(-1, user, new Date, Add(), newTask))
            Ok(generate(Map("status" -> m.views.tasks.successfullyAdded,
              "task" -> task2view(newTask))))
          }
        )
        case None => BadRequest(toJson(
          Map("status" -> "ERROR",
            "cause" -> ("Failed to parse body as JSON: " + request.body.asText.getOrElse(request.body.toString)))
        ))
      }
  }

  def delete(id: Long) = AssetMngAction {
    (user, request) =>
      val oldTask = assetTasksDB.delete(id)
      activityDB.save(new HistoryEntry(-1, user, new Date, Delete(), oldTask))
      Ok(toJson(Map("status" -> "OK")))
  }
}