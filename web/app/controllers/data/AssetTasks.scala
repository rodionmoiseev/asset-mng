package controllers.data

import play.api.mvc._
import com.codahale.jerkson.Json._
import java.util.Date
import controllers.{IPUtils, SimpleAssetsDAO, SimpleAssetStateDAO}
import models.view._
import play.api.libs.json.Json._
import scala.Some
import play.api.data.Form
import play.api.data.Forms._
import scala.Some
import models.view.ViewAssetTaskGroup
import scala.Some
import models.view.ViewAssetTask
import models.{AssetTask, AssetState}
import i18n.Messages

/**
 *
 * @author rodion
 */

object AssetTasks extends Controller {
  implicit def m: Messages = Messages.m

  case class NewTask(hostname: String, description: String, user: String, tags: String)

  val taskForm = Form(mapping(
    "hostname" -> nonEmptyText,
    "description" -> text,
    "user" -> text,
    "tags" -> text)
    (NewTask.apply)(NewTask.unapply))

  def task2view = (task: models.AssetTask) =>
    ViewAssetTask(task.id, task.user, task.desc, task.dateStr, task.tags)

  def form2task = (taskForm: NewTask) =>
    AssetTask(123L, taskForm.user, taskForm.description, new Date(), taskForm.tags.split(", ").toList)

  def state2asset = (state: models.AssetState) =>
    ViewAssetTaskGroup(Assets.asset2view(state.asset), state.tasks map task2view)

  def groupedByAsset = Action {
    Ok(generate(SimpleAssetStateDAO.get.list map state2asset))
  }

  def add = Action {
    implicit request =>
      request.body.asJson match {
        case Some(json) => taskForm.bind(json).fold(
          errors => BadRequest(errors.errorsAsJson),
          viewTask => {
            SimpleAssetsDAO.me.list.find((asset)=> asset.hostname == viewTask.hostname) match {
              case Some(asset) => SimpleAssetStateDAO.me.addTask(asset, form2task(viewTask))
            }
            Ok(generate(Map("status" -> m.views.tasks.successfullyAdded,
                          "task" -> viewTask)))
          }
        )
        case None => BadRequest(toJson(
          Map("status" -> "ERROR",
            "cause" -> ("Failed to parse body as JSON: " + request.body.asText.getOrElse(request.body.toString)))
        ))
      }
  }
}
