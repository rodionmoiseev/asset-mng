package dao

import filestorage._
import models.{HistoryEntry, Persistent, Asset, AssetTask}
import java.util.concurrent.atomic.AtomicLong
import collection.mutable

object Module {
  implicit val assetTasksDB: DB[AssetTask] = concurrent(new JsonAssetTasksDB("db/tasks.csv", "UTF-8"))
  implicit val assetsDB: DB[Asset] = concurrent(new JsonAssetsDB("db/assets.csv", "UTF-8"))
  implicit val activityDB: DB[HistoryEntry] = concurrent(new JsonActivityDB("db/activity.csv", "UTF-8"))

  private def concurrent[A <: Persistent[A]](base: DB[A]): DB[A] = {
    new SingleThreadedDB[A](base)
  }
}

class InMemoryDB[A <: Persistent[A]] extends DB[A] {
  var uniqId: AtomicLong = new AtomicLong

  def nextUniqId: Long = uniqId.incrementAndGet

  val items: mutable.MutableList[A] = new mutable.MutableList[A]

  def all = items.toList

  def save(item: A): A = {
    val newItem = item.withId(nextUniqId)
    items += newItem
    newItem
  }

  def delete(id: Long): A = {
    throw new UnsupportedOperationException("delete is not supported")
  }
}

class InMemoryAssetTasksDB extends InMemoryDB[AssetTask] with AssetTasksDB

class InMemoryAssetsDB extends InMemoryDB[Asset] with AssetsDB