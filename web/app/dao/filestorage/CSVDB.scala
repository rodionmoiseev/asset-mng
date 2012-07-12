package dao.filestorage

import dao._
import models._
import com.codahale.jerkson.Json._
import collection.mutable.ListBuffer
import play.api.libs.json._
import play.api.libs.json.Json.toJson
import models.Asset
import models.HistoryEntry
import scala.Some
import models.Modify
import models.Delete
import models.Add
import models.AssetTask
import java.util.Date

/**
 *
 * @author rodion
 */

abstract class CSVDB[A <: Persistent[A]](val file: String, val enc: String) extends DB[A] {
  val frw = new SafeFileWriter(file, enc)
  private val items: ListBuffer[A] = new ListBuffer[A]
  private var synced = false

  def all: List[A] = {
    sync() //Sync file-memory state
    items.toList
  }

  private def sync() {
    if (!synced) {
      val data = frw.read() match {
        case Some(data) => read(data)
        case None => List()
      }
      items ++= data
      synced = true
    }
  }

  def save(item: A): A = {
    val newItem = item.withId(items.length)
    items += newItem
    write()
    newItem
  }

  def delete(id: Long): A = {
    val item = items.remove(items.indexWhere {
      _.id == id
    })
    write()
    item
  }

  private def write() {
    frw.write(write(items.toList))
  }

  def write(data: List[A]): String = generate(data)

  def read(data: String): List[A]
}

class CSVAssetsDB(file: String, enc: String) extends CSVDB[Asset](file, enc) with AssetsDB {
  def read(data: String) = parse[List[Asset]](data)
}

class CSVAssetTasksDB(file: String, enc: String) extends CSVDB[AssetTask](file, enc) with AssetTasksDB {
  def read(data: String) = parse[List[AssetTask]](data)
}

class CSVActivityDB(file: String, enc: String) extends CSVDB[HistoryEntry](file, enc) with ActivityDB {
  def read(data: String): List[HistoryEntry] =
    Json.parse(data).as[Seq[JsValue]].map(_.as[HistoryEntry](new HistoryEntryReads)).toList

  override def write(data: List[HistoryEntry]) = Json.stringify(Json.toJson(
    data map {
      Json.toJson(_)(new HistoryEntryWrites)
    }
  ))
}

class HistoryEntryWrites extends Writes[HistoryEntry] {
  def writes(entry: HistoryEntry) = Json.toJson(Map(
    "id" -> toJson(entry.id),
    "user" -> toJson(entry.user),
    "date" -> toJson(entry.date.getTime),
    "action" -> toJson(entry.action)(new HistoryActionWrites),
    "obj" -> toJson(entry.obj)(new HistoryObjectWrites)
  ))
}

class HistoryEntryReads extends Reads[HistoryEntry] {
  def reads(json: JsValue) = HistoryEntry(
    (json \ "id").as[Long],
    (json \ "user").as[String],
    new Date((json \ "date").as[Long]),
    (json \ "action").as[HistoryAction](new HistoryActionReads),
    (json \ "obj").as[HistoryObject](new HistoryObjectReads)
  )
}

class HistoryActionWrites extends Writes[HistoryAction] {
  def writes(action: HistoryAction) = Json.toJson(Map(
    "type" -> action.getClass.getSimpleName.toLowerCase
  ))
}

class HistoryActionReads extends Reads[HistoryAction] {
  def reads(json: JsValue) = (json \ "type").as[String] match {
    case "add" => Add()
    case "modify" => Modify()
    case "delete" => Delete()
  }
}

class HistoryObjectWrites extends Writes[HistoryObject] {
  def writes(obj: HistoryObject) = Json.toJson(Map(
    "type" -> toJson(obj.getClass.getSimpleName.toLowerCase),
    "obj" -> toJson(generate(obj))
  ))
}

class HistoryObjectReads extends Reads[HistoryObject] {
  def reads(json: JsValue) = (json \ "type").as[String] match {
    case "asset" => parse[Asset]((json \ "obj").as[String])
  }
}