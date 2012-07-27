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

  private def collectTags(): Set[String] = {
    assetTasksDB.all.toSet.map((task: AssetTask) => task.tags).flatten
  }
}
