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
import util.Date
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
      asset.tags.getOrElse(List()),
      asset.parent_id,
      assetUsageStatus(asset, m),
      status2view(assetStatusSystem.getStatus(asset), m))

  def status2view = (status: AssetStatus, m: Messages) => {
    val ms = m.views.assets.status
    val stat = status.status match {
      case "checking" => (ms.checkingTitle, ms.checking(status.asset.ip))
      case "ok" => (ms.okTitle, ms.ok(status.asset.ip))
      case "unreachable" => (ms.unreachableTitle, ms.unreachable(status.asset.ip))
      case _ => (ms.errorTitle, ms.error(status.asset.ip, status.error))
    }
    ViewAssetStatus(status.status, stat._1, stat._2, status.lastCheckedStr)
  }

  def assetUsageStatus(asset: Asset, m: Messages) =
    assetTasksDB.all.find(_.asset_id == asset.id).map(task => m.asset.used).getOrElse(m.asset.available)

  def form2asset = (asset: AssetForm) =>
    Asset(
      asset.id,
      asset.hostname,
      asset.ip,
      asset.description,
      asset.admin,
      Some(Tags.toDelimitedList(asset.tags)),
      asset.parent_id)

  case class AssetForm(id: Long,
                       hostname: String,
                       ip: String,
                       description: String,
                       admin: String,
                       tags: String,
                       parent_id: Option[Long])

  val assetForm = Form(mapping(
    "id" -> longNumber,
    "hostname" -> nonEmptyText,
    "ip" -> nonEmptyText.verifying("Must be a valid IPv4/v6 address", IPUtils.isIPAddress _),
    "description" -> text,
    "admin" -> text,
    "tags" -> text,
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
            Ok(generate(Map("status" -> ctx.m.views.assets.successfullyAdded(viewAsset.hostname),
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

  def update = AssetMngAction {
    implicit ctx =>
      ctx.request.body.asJson match {
        case Some(json) => assetForm.bind(json).fold(
          errors => BadRequest(errors.errorsAsJson),
          viewAsset => {
            val newAsset = form2asset(viewAsset)
            updateAsset(newAsset, ctx.user)
            Ok(generate(Map("status" -> ctx.m.views.assets.successfullyUpdated(newAsset.hostname),
              "asset" -> asset2view(newAsset, ctx.m))))
          }
        )
        case None => BadRequest(toJson(
          Map("status" -> "ERROR",
            "cause" -> ("Failed to parse body as JSON: " + ctx.request.body.asText.getOrElse(ctx.request.body.toString)))
        ))
      }
  }

  def updateAsset(asset: Asset, user: String, action: HistoryAction = Modify()): HistoryEntry = {
    val oldItem = assetsDB.update(asset)
    activityDB.save(new HistoryEntry(DB.NEW_ID, user, new Date, action, oldItem))
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
