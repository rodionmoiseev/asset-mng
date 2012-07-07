package dao

import models.{Persistent, Asset, AssetTask}
import java.util.concurrent.atomic.AtomicLong
import collection.mutable

//just to stop IntelliJ from complaining
object Module {
  implicit val assetTasksDB: AssetTasksDB = new InMemoryAssetTasksDB
  implicit val assetsDB: AssetsDB = new InMemoryAssetsDB
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
}

class InMemoryAssetTasksDB extends InMemoryDB[AssetTask] with AssetTasksDB

class InMemoryAssetsDB extends InMemoryDB[Asset] with AssetsDB