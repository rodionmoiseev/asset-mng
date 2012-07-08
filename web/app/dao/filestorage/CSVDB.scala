package dao.filestorage

import dao.{AssetTasksDB, AssetsDB, DB}
import java.util.concurrent.atomic.AtomicLong
import models.Persistent
import collection.mutable
import models.AssetTask
import scala.Some
import models.Asset
import com.codahale.jerkson.Json._

/**
 *
 * @author rodion
 */

abstract class CSVDB[A <: Persistent[A]](val file: String, val enc: String) extends DB[A] {
  val frw = new SafeFileWriter(file, enc)
  var uniqId: AtomicLong = new AtomicLong
  private val items: mutable.MutableList[A] = new mutable.MutableList[A]
  private var synced = false

  def nextUniqId: Long = uniqId.incrementAndGet

  def all: List[A] = {
    sync() //Sync file-memory state
    items.toList
  }

  private def sync() {
    if (!synced) {
      val data = frw.read() match {
        case Some(data) => convert(data)
        case None => List()
      }
      items ++= data
      synced = true
    }
  }

  def save(item: A): A = {
    val newItem = item.withId(nextUniqId)
    items += newItem
    frw.write(generate(items.toList))
    newItem
  }

  def convert(data: String): List[A]
}

class CSVAssetsDB(file: String, enc: String) extends CSVDB[Asset](file, enc) with AssetsDB {
  def convert(data: String) = parse[List[Asset]](data)
}

class CSVAssetTasksDB(file: String, enc: String) extends CSVDB[AssetTask](file, enc) with AssetTasksDB {
  def convert(data: String) = parse[List[AssetTask]](data)
}