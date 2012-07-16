package controllers.data

import com.codahale.jerkson.Json._
import play.api.libs.json.Json.toJson

import play.api.mvc._
import models._
import models.view.ViewAsset
import controllers.IPUtils
import play.api.data._
import play.api.data.Forms._
import i18n.Messages
import dao.Module._
import java.util
import controllers.Application.AssetMngAction
import dao.DB
import models.Asset
import models.HistoryEntry
import scala.Some
import models.Delete
import view.ViewAsset
import models.Add

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
            val hist = addAsset(form2asset(viewAsset), user)
            Ok(generate(Map("status" -> m.views.assets.successfullyAdded,
              "asset" -> asset2view(hist.obj.asInstanceOf[Asset]))))
          }
        )
        case None => BadRequest(toJson(
          Map("status" -> "ERROR",
            "cause" -> ("Failed to parse body as JSON: " + request.body.asText.getOrElse(request.body.toString)))
        ))
      }
  }

  def addAsset(asset: Asset, user: String, action: HistoryAction = Add()): HistoryEntry = {
    val newAsset = assetsDB.save(asset)
    activityDB.save(HistoryEntry(DB.NEW_ID, user, new util.Date, action, newAsset))
  }

  def delete(id: Long) = AssetMngAction {
    (user, request) =>
      deleteAsset(id, user)
      Ok(toJson(Map("status" -> "OK")))
  }

  def deleteAsset(id: Long, user: String, action: HistoryAction = Delete()): HistoryEntry = {
    val deletedItem = assetsDB.delete(id)
    assetTasksDB.all.filter(_.asset_id == id).foreach {
      (task) => {
        AssetTasks.deleteTask(task.id, user, action)
      }
    }
    activityDB.save(HistoryEntry(DB.NEW_ID, user, new util.Date, action, deletedItem))
  }
}
