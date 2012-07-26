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
import dao.Module._
import controllers.Application.AssetMngAction
import dao.DB

/**
 *
 * @author rodion
 */

object AssetTasks extends Controller {

  case class AssetTaskForm(asset_id: Long, description: String, tags: String, icons: String)

  val taskForm = Form(mapping(
    "asset_id" -> longNumber,
    "description" -> nonEmptyText,
    "tags" -> text,
    "icons" -> text)
    (AssetTaskForm.apply)(AssetTaskForm.unapply))

  def task2view = (task: models.AssetTask) =>
    ViewAssetTask(task.id, task.asset_id, task.user, task.description, task.dateStr, task.tags, task.icons)

  def form2task = (taskForm: AssetTaskForm, user: String) =>
    AssetTask(
      DB.NEW_ID,
      taskForm.asset_id,
      user,
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
    implicit ctx =>
      val tasks = assetTasksDB.all
      val groups = assetsDB.all map {
        (asset) => (asset, tasks.filter((task) => task.asset_id == asset.id))
      }
      Ok(generate(groups map asTaskGroup))
  }

  def add = AssetMngAction {
    implicit ctx =>
      ctx.request.body.asJson match {
        case Some(json) => taskForm.bind(json).fold(
          errors => BadRequest(errors.errorsAsJson),
          viewTask => {
            val hist = addTask(form2task(viewTask, ctx.user), ctx.user)
            Ok(generate(Map("status" -> ctx.m.views.tasks.successfullyAdded,
              "task" -> task2view(hist.obj.asInstanceOf[AssetTask]))))
          }
        )
        case None => BadRequest(toJson(
          Map("status" -> "ERROR",
            "cause" -> ("Failed to parse body as JSON: " + ctx.request.body.asText.getOrElse(ctx.request.body.toString)))
        ))
      }
  }

  def addTask(task: AssetTask, user: String, action: HistoryAction = Add()): HistoryEntry = {
    val newTask = assetTasksDB.save(task)
    activityDB.save(new HistoryEntry(DB.NEW_ID, user, new Date, Add(), newTask))
  }

  def delete(id: Long) = AssetMngAction {
    implicit ctx =>
      deleteTask(id, ctx.user)
      Ok(toJson(Map("status" -> "OK")))
  }

  def deleteTask(id: Long, user: String, action: HistoryAction = Delete()): HistoryEntry = {
    val oldTask = assetTasksDB.delete(id)
    activityDB.save(new HistoryEntry(DB.NEW_ID, user, new Date, action, oldTask))
  }
}