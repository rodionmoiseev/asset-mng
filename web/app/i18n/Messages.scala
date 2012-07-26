package i18n

import c10n.annotations.{Ja, En}
import c10n.C10NMessages
import context.AssetMngContext

/**
 *
 * @author rodion
 */
object Messages {
  implicit val req2messages: (AssetMngContext => Messages) = _.m
}

@C10NMessages
trait Messages {
  def main: Main

  def login: Login

  def buttons: Buttons

  def asset: Asset

  def task: Task

  def errors: Errors

  def views: Views

  def activity: Activity
}

@C10NMessages
trait Main {
  @En("Logout ({0})")
  @Ja("ログアウト ({0})")
  def logoutLink(userName: String): String

  @En("Login")
  @Ja("ログイン")
  def login: String
}

@C10NMessages
trait Login {
  @En("Asset Manager")
  @Ja("Asset Manager")
  def header: String

  @En("Please log-in")
  @Ja("ログインしてください")
  def message: String

  @En("User name")
  @Ja("ユーザ名")
  def userName: String

  @En("Login")
  @Ja("ログイン")
  def button: String
}

@C10NMessages
trait Views {
  def assets: Assets

  def tasks: Tasks

  def activity: ActivityView
}

@C10NMessages
trait Assets {
  @En("Manage Assets")
  @Ja("アセット管理")
  def title: String

  @En("Add new asset")
  @Ja("新規アセット追加")
  def addAsset: String

  @En("Import assets")
  @Ja("アセットのインポート")
  def importAssets: String

  @En("New asset was added")
  @Ja("アセットを登録しました")
  def successfullyAdded: String

  @En("Delete")
  @Ja("削除")
  def delete: String
}

@C10NMessages
trait Tasks {
  @En("Tasks")
  @Ja("タスク")
  def title: String

  @En("Add Task")
  @Ja("タスク追加")
  def addTask: String

  @En("New task was create")
  @Ja("タスクを作成しました")
  def successfullyAdded: String

  @En("Delete")
  @Ja("削除")
  def delete: String
}

@C10NMessages
trait ActivityView {
  @En("Recent Activity")
  @Ja("近況")
  def title: String

  @En("Undo")
  @Ja("元に戻す")
  def undo: String
}

@C10NMessages
trait Buttons {
  @En("New Asset")
  @Ja("アセット追加")
  def newAsset: String

  @En("Save")
  @Ja("保存")
  def save: String

  @En("Cancel")
  @Ja("キャンセル")
  def cancel: String
}

@C10NMessages
trait Asset {
  @En("Assets")
  @Ja("アセット")
  def title: String

  @En("Hostname")
  @Ja("ホスト名")
  def hostname: String

  @En("Name")
  @Ja("登録名")
  def name: String

  @En("IP address")
  @Ja("IPアドレス")
  def ip: String

  @En("Description")
  @Ja("概要")
  def description: String

  @En("Admin")
  @Ja("管理者")
  def admin: String

  @En("Not used by anyone")
  @Ja("空いています")
  def available: String

  @En("{0} asset")
  @Ja("{0}アセット")
  def describe(hostname: String): String
}

@C10NMessages
trait Task {
  @En("Currently used assets")
  @Ja("アセット使用状況")
  def title: String

  @En("User")
  @Ja("ユーザ")
  def user: String

  @En("Description")
  @Ja("概要")
  def description: String

  @En("Tags")
  @Ja("タグ")
  def tags: String

  @En("Task list")
  @Ja("タスク")
  def taskList: String

  @En("Icons")
  @Ja("アイコン")
  def icons: String

  @En("Task \"{1} ...\" by {0}")
  @Ja("{0}さんの\"{1} ...\"タスク")
  def describe(user: String, briefDescription: String): String
}

@C10NMessages
trait Errors {
  @En("Not a valid IP address")
  @Ja("不正なIPアドレス")
  def invalidIP: String

  @En("<strong>Asset could not be saved: </strong> Please correct problems below and click 'Save'")
  @Ja("<strong>アセット保存に失敗しました: </strong> " +
    "入力内容を修正し「保存」ボタンをクリックしてください")
  def formValidationError: String
}

@C10NMessages
trait Activity {
  @En("{0} has been {1}")
  @Ja("{0}が{1}されました")
  def log(obj: String, action: String): String

  @En("added")
  @Ja("追加")
  def added: String

  @En("addition")
  @Ja("追加")
  def addition: String

  @En("modified")
  @Ja("編集")
  def modified: String

  @En("modification")
  @Ja("編集")
  def modification: String

  @En("deleted")
  @Ja("削除")
  def deleted: String

  @En("deletion")
  @Ja("削除")
  def deletion: String

  @En("undone")
  @Ja("{0}のUNDO")
  def undone(action: String): String

  @En("undo")
  @Ja("UNDO")
  def undo: String

  @En("asset")
  @Ja("アセット")
  def assetEntry: String

  @En("task")
  @Ja("タスク")
  def assetTaskEntry: String
}