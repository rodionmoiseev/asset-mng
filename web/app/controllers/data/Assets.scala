package controllers.data

import com.codahale.jerkson.Json._
import play.api.libs.json.Json.toJson

import play.api.mvc._
import models.Asset
import models.view.ViewAsset
import controllers.{IPUtils, SimpleAssetsDAO}
import play.api.data._
import play.api.data.Forms._
import i18n.Messages

/**
 *
 * @author rodion
 */

object Assets extends Controller {
  val m: Messages = Messages.m

  def asset2view = (asset: Asset) =>
    ViewAsset(asset.hostname,
      asset.ip,
      asset.description,
      asset.admin,
      asset.tags.mkString(", "))

  def view2asset = (asset: ViewAsset) =>
    Asset(asset.hostname,
      asset.ip,
      asset.description,
      asset.admin,
      asset.tags.split(",").toList)

  val assetForm = Form(mapping(
    "hostname" -> nonEmptyText,
    "ip" -> nonEmptyText.verifying("Must be a valid IPv4/v6 address", IPUtils.isIPAddress _),
    "description" -> text,
    "admin" -> text,
    "tags" -> text)
    (ViewAsset.apply)(ViewAsset.unapply))

  def list = Action {
    Ok(generate(SimpleAssetsDAO.me.list map asset2view))
  }

  def add = Action {
    implicit request =>
      request.body.asJson match {
        case Some(json) => assetForm.bind(json).fold(
          errors => BadRequest(errors.errorsAsJson),
          viewAsset => {
            SimpleAssetsDAO.me.save(view2asset(viewAsset))
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
}
