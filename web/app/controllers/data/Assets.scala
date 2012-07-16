package controllers.data

import com.codahale.jerkson.Json._
import play.api.libs.json.Json.toJson

import play.api.mvc._
import models.{Delete, Add, HistoryEntry, Asset}
import models.view.ViewAsset
import controllers.IPUtils
import play.api.data._
import play.api.data.Forms._
import i18n.Messages
import dao.Module._
import java.util
import controllers.Application.AssetMngAction
import dao.DB

/**
 *
 * @author rodion
 */

object Assets extends Controller {
  val m: Messages = Messages.m

  def asset2view = (asset: Asset) =>
    ViewAsset(
      asset.id,
      asset.hostname,
      asset.ip,
      asset.description,
      asset.admin,
      asset.parent_id)

  def form2asset = (asset: AssetForm) =>
    Asset(
      DB.NEW_ID,
      asset.hostname,
      asset.ip,
      asset.description,
      asset.admin,
      asset.parent_id)

  case class AssetForm(hostname: String,
                       ip: String,
                       description: String,
                       admin: String,
                       parent_id: Option[Long])

  val assetForm = Form(mapping(
    "hostname" -> nonEmptyText,
    "ip" -> nonEmptyText.verifying("Must be a valid IPv4/v6 address", IPUtils.isIPAddress _),
    "description" -> text,
    "admin" -> text,
    "parent_id" -> optional(longNumber))
    (AssetForm.apply)(AssetForm.unapply))

  def list = AssetMngAction {
    (user, request) =>
      Ok(generate(assetsDB.all map asset2view))
  }

  def add = AssetMngAction {
    (user, request) =>
      request.body.asJson match {
        case Some(json) => assetForm.bind(json).fold(
          errors => BadRequest(errors.errorsAsJson),
          viewAsset => {
            val newAsset = assetsDB.save(form2asset(viewAsset))
            activityDB.save(HistoryEntry(DB.NEW_ID, user, new util.Date, Add(), newAsset))
            Ok(generate(Map("status" -> m.views.assets.successfullyAdded,
              "asset" -> viewAsset)))
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
      val deletedItem = assetsDB.delete(id)
      assetTasksDB.all.filter(_.asset_id == id).foreach {
        (task) => {
          println("transitively removing task: " + task.id)
          AssetTasks.deleteTask(task.id, user)
        }
      }
      activityDB.save(HistoryEntry(DB.NEW_ID, user, new util.Date, Delete(), deletedItem))
      Ok(toJson(Map("status" -> "OK")))
  }
}
