package controllers.data

import com.codahale.jerkson.Json._

import play.api.mvc._
import models._
import dao.Module._
import controllers.Application.AssetMngAction

/**
 *
 * @author rodion
 */

object Tags extends Controller {
  def list = AssetMngAction {
    implicit ctx =>
      Ok(generate(collectTags()))
  }

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

  private def collectTags(): Set[String] = {
    assetTasksDB.all.toSet.map((task: AssetTask) => task.tags).flatten ++
    assetsDB.all.toSet.map((asset: Asset) => asset.tags.getOrElse(List())).flatten
  }
}
