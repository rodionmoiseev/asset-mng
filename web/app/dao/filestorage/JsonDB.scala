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

abstract class JsonDB[A <: Persistent[A]](val file: String, val enc: String) extends DB[A] {
  val frw = new SafeFileWriter(file, enc)
  protected val items: ListBuffer[A] = new ListBuffer[A]
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
    val newItem = item.withId(nextUniqueId)
    items += newItem
    flush()
    newItem
  }

  def delete(id: Long): A = {
    val item = items.remove(items.indexWhere {
      _.id == id
    })
    flush()
    item
  }

  protected def flush() {
    frw.write(write(items.toList))
  }

  def nextUniqueId: Long

  def write(data: List[A]): String = generate(data)

  def read(data: String): List[A]
}

abstract class JsonDBWithIdProvider[A <: Persistent[A]](file: String, enc: String, val idProvider: DB[UniqueId])
  extends JsonDB[A](file, enc) {
  def nextUniqueId = {
    val nextId = idProvider.all.head.id
    idProvider.save(new UniqueId(nextId + 1))
    nextId
  }
}

class JsonUniqueUniqueIdDB(file: String, enc: String) extends JsonDB[UniqueId](file, enc) with UniqueIdDB {
  items += new UniqueId(0)

  def read(data: String) = parse[List[UniqueId]](data)

  def nextUniqueId = all.head.id

  override def save(newItem: UniqueId) = {
    items.update(0, newItem)
    flush()
    newItem
  }
}

class JsonAssetsDB(file: String, enc: String, idProvider: DB[UniqueId]) extends JsonDBWithIdProvider[Asset](file, enc, idProvider) with AssetsDB {
  def read(data: String) = parse[List[Asset]](data)
}

class JsonAssetTasksDB(file: String, enc: String, idProvider: DB[UniqueId]) extends JsonDBWithIdProvider[AssetTask](file, enc, idProvider) with AssetTasksDB {
  def read(data: String) = parse[List[AssetTask]](data)
}

class JsonActivityDB(file: String, enc: String, idProvider: DB[UniqueId]) extends JsonDBWithIdProvider[HistoryEntry](file, enc, idProvider) with ActivityDB {
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
    case "assettask" => parse[AssetTask]((json \ "obj").as[String])
  }
}