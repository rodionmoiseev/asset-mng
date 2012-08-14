package dao

import filestorage._
import models._
import java.util.concurrent.atomic.AtomicLong
import models.Asset
import models.HistoryEntry
import models.AssetTask

object Module {
  private val uidProvider: UIDProvider = new MillisecBasedUIDProvider
  implicit val assetTasksDB: DB[AssetTask] = concurrent(new JsonAssetTasksDB("db/tasks.json", "UTF-8", uidProvider))
  implicit val assetsDB: DB[Asset] = concurrent(new JsonAssetsDB("db/assets.json", "UTF-8", uidProvider))
  implicit val activityDB: DB[HistoryEntry] = concurrent(new JsonActivityDB("db/activity.json", "UTF-8", uidProvider))

  private def concurrent[A <: Persistent[A]](base: DB[A]): DB[A] = {
    new SingleThreadedDB[A](base)
  }
}

class MillisecBasedUIDProvider extends UIDProvider {
  val uid: AtomicLong = new AtomicLong(System.currentTimeMillis())

  def nextUID = uid.getAndIncrement()
}