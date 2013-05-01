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

abstract class JsonDB[A <: Persistent[A]](val file: String, val enc: String, val uidProvider: UIDProvider) extends DB[A] {
  val frw = new SafeFileWriter(file, enc)
  protected val items: ListBuffer[A] = new ListBuffer[A]
  private var synced = false

  def all: List[A] = {
    sync() //Sync file-memory state
    items.toList.sortWith(ordering)
  }

  private def sync() {
    if (!synced) {
      val data = frw.read() match {
        case Some(d) => read(d)
        case None => List()
      }
      items ++= data
      synced = true
    }
  }

  def save(item: A): A = {
    val newItem = item.id match {
      case DB.NEW_ID => item.withId(uidProvider.nextUID)
      //Keep the old uid (mainly needed when reverting delete)
      case _ => item
    }
    items += newItem
    flush()
    newItem
  }

  def update(item: A): A = {
    val oldItem = delete(item.id)
    save(item)
    oldItem
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

  def write(data: List[A]): String = generate(data)

  def read(data: String): List[A]

  def ordering: (A, A) => Boolean = _.id < _.id
}

class JsonAssetsDB(file: String, enc: String, idProvider: UIDProvider) extends JsonDB[Asset](file, enc, idProvider) with AssetsDB {
  def read(data: String) = parse[List[Asset]](data)

  override def ordering = _.hostname < _.hostname
}

class JsonAssetTasksDB(file: String, enc: String, idProvider: UIDProvider) extends JsonDB[AssetTask](file, enc, idProvider) with AssetTasksDB {
  def read(data: String) = parse[List[AssetTask]](data)
}

class JsonActivityDB(file: String, enc: String, idProvider: UIDProvider) extends JsonDB[HistoryEntry](file, enc, idProvider) with ActivityDB {
  val MAX_HISTORY_SIZE = Integer.getInteger("assetmng.activity.maxHistorySize", 100)

  override def all = super.all.takeRight(MAX_HISTORY_SIZE)

  def read(data: String): List[HistoryEntry] =
    Json.parse(data).as[Seq[JsValue]].map(_.as[HistoryEntry](new HistoryEntryReads)).toList

  override def write(data: List[HistoryEntry]) = Json.stringify(Json.toJson(
    data.takeRight(MAX_HISTORY_SIZE) map {
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
  def reads(json: JsValue) = JsSuccess(HistoryEntry(
    (json \ "id").as[Long],
    (json \ "user").as[String],
    new Date((json \ "date").as[Long]),
    (json \ "action").as[HistoryAction](new HistoryActionReads),
    (json \ "obj").as[HistoryObject](new HistoryObjectReads)
  ))
}

class HistoryActionWrites extends Writes[HistoryAction] {
  def writes(action: HistoryAction) = Json.toJson(Map(
    "type" -> toJson(action.getClass.getSimpleName.toLowerCase),
    "undoAction" -> undoAction(action)
  ))

  def undoAction(action: HistoryAction): JsValue = {
    action match {
      case undoAction: Undo => writes(undoAction.action)
      case _ => JsNull
    }
  }
}

class HistoryActionReads extends Reads[HistoryAction] {
  def reads(json: JsValue): JsResult[HistoryAction] = (json \ "type").as[String] match {
    case "add" => JsSuccess(Add())
    case "modify" => JsSuccess(Modify())
    case "delete" => JsSuccess(Delete())
    case "undo" => reads(json \ "undoAction")
    case unknown => JsError("Unknown field %s".format(unknown))
  }
}

class HistoryObjectWrites extends Writes[HistoryObject] {
  def writes(obj: HistoryObject) = Json.toJson(Map(
    "type" -> toJson(obj.getClass.getSimpleName.toLowerCase),
    "obj" -> obj2json(obj)
  ))

  private def obj2json(obj: HistoryObject): JsValue = {
    obj match {
      case undoEntry: UndoEntry => toJson(undoEntry)(new UndoEntryWrites)
      case _ => toJson(generate(obj))
    }
  }
}

class HistoryObjectReads extends Reads[HistoryObject] {
  def reads(json: JsValue): JsResult[HistoryObject] = (json \ "type").as[String] match {
    case "asset" => JsSuccess(parse[Asset]((json \ "obj").as[String]))
    case "assettask" => JsSuccess(parse[AssetTask]((json \ "obj").as[String]))
    case "undoentry" => JsSuccess(json.as[UndoEntry](new UndoEntryReads))
    case unknown => JsError("Unknown field %s".format(unknown))
  }
}

class UndoEntryWrites extends Writes[UndoEntry] {
  def writes(undoEntry: UndoEntry) = Json.toJson(Map(
    "entryType" -> toJson(undoEntry.entryType.getClass.getSimpleName.toLowerCase)
  ))
}

class UndoEntryReads extends Reads[UndoEntry] {
  def reads(json: JsValue) = JsSuccess(UndoEntry(
    (json \ "entryType").as[String] match {
      case "assetentry" => AssetEntry()
      case "assettaskentry" => AssetTaskEntry()
    }
  ))
}