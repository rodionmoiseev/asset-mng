package controllers

import models.{Asset, AssetTask, AssetState}
import java.util.Date


/**
 *
 * @author rodion
 */

trait AssetStateDAO {
  def updateDesc(id: Long, desc: String)
  def add(assetState: AssetState)
  def list(): List[AssetState]
}

object SimpleAssetStateDAO{
  val me = new SimpleAssetStateDAO
  def get = me
}

class SimpleAssetStateDAO extends AssetStateDAO{
  var tasks: List[AssetTask] = List(
    AssetTask(1, "konagaya", "working hard", new Date, List("tag1")),
    AssetTask(2, "rodion", "benchmarking\nVer3.4 and X", new Date, List("tag1", "tag2"))
  )

  var assetsUsage: List[AssetState] = List()

  def updateDesc(id: Long, desc: String) {
    tasks = tasks.map { task =>
      if (task.id == id)
        task.copy(desc = desc)
      else
        task
    }
  }

  def add(assetState: AssetState) {
    assetsUsage = assetState :: assetsUsage
  }

  def addTask(asset: Asset, task: AssetTask) {
    assetsUsage.find((state) => state.asset.hostname == asset.hostname) match {
      case Some(state) => state.tasks = task :: state.tasks
      case None => println("DEBUG: asset with hostname=" + asset.hostname + " could not be found")
    }
  }

  def list() = SimpleAssetsDAO.me.list map { asset => AssetState(asset, "Used", tasks) }
}
