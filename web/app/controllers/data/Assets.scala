package controllers.data

import com.codahale.jerkson.Json._
import play.api.libs.json.Json.toJson

import play.api.mvc._
import models._
import controllers.IPUtils
import play.api.data._
import play.api.data.Forms._
import dao.Module._
import java.util
import controllers.Application.AssetMngAction
import dao.DB
import models.Asset
import models.HistoryEntry
import scala.Some
import models.Delete
import view.{ViewAssetStatus, ViewAsset}
import models.Add
import assetstatus.Module._
import assetstatus.AssetStatus
import i18n.Messages

/**
 *
 * @author rodion
 */

object Assets extends Controller {
  def asset2view = (asset: Asset, m: Messages) =>
    ViewAsset(
      asset.id,
      asset.hostname,
      asset.ip,
      asset.description,
      asset.admin,
      asset.parent_id,
      status2view(assetStatusSystem.getStatus(asset), m))

  def status2view = (status: AssetStatus, m: Messages) => {
    val ms = m.views.assets.status
    val stat = status.status match {
      case "checking" =>(ms.checkingTitle, ms.checking(status.asset.ip))
      case "ok" => (ms.okTitle, ms.ok(status.asset.ip))
      case "unreachable" => (ms.unreachableTitle, ms.unreachable(status.asset.ip))
      case _ => (ms.errorTitle, ms.error(status.asset.ip, status.error))
    }
    ViewAssetStatus(status.status, stat._1, stat._2, status.lastCheckedStr)
  }

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
    implicit ctx =>
      Ok(generate(assetsDB.all map (asset2view(_, ctx.m))))
  }

  def add = AssetMngAction {
    implicit ctx =>
      ctx.request.body.asJson match {
        case Some(json) => assetForm.bind(json).fold(
          errors => BadRequest(errors.errorsAsJson),
          viewAsset => {
            val hist = addAsset(form2asset(viewAsset), ctx.user)
            Ok(generate(Map("status" -> ctx.m.views.assets.successfullyAdded,
              "asset" -> asset2view(hist.obj.asInstanceOf[Asset], ctx.m))))
          }
        )
        case None => BadRequest(toJson(
          Map("status" -> "ERROR",
            "cause" -> ("Failed to parse body as JSON: " + ctx.request.body.asText.getOrElse(ctx.request.body.toString)))
        ))
      }
  }

  def addAsset(asset: Asset, user: String, action: HistoryAction = Add()): HistoryEntry = {
    val newAsset = assetsDB.save(asset)
    activityDB.save(HistoryEntry(DB.NEW_ID, user, new util.Date, action, newAsset))
  }

  def delete(id: Long) = AssetMngAction {
    implicit ctx =>
      deleteAsset(id, ctx.user)
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
