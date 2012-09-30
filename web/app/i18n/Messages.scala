package i18n

import c10n.annotations.{Ru, Ja, En}
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
  @Ru("Выход ({0})")
  def logoutLink(userName: String): String

  @En("Login")
  @Ja("ログイン")
  @Ru("Войти")
  def login: String
}

@C10NMessages
trait Login {
  @En("Asset Manager")
  def header: String

  @En("Please log-in")
  @Ja("ログインしてください")
  @Ru("Пожалуйста войдите")
  def message: String

  @En("User name")
  @Ja("ユーザ名")
  @Ru("Имя пользователя")
  def userName: String

  @En("Login")
  @Ja("ログイン")
  @Ru("Вход")
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
  def status: Status

  @En("Manage Assets")
  @Ja("アセット管理")
  @Ru("Активы")
  def title: String

  @En("Add new asset")
  @Ja("新規アセット追加")
  @Ru("Добавить актив")
  def addAsset: String

  @En("Import assets")
  @Ja("アセットのインポート")
  @Ru("Импортировать активы")
  def importAssets: String

  @En("New asset {0} was added")
  @Ja("{0} アセットを登録しました")
  @Ru("Новый актив {0} был добавлен")
  def successfullyAdded(hostname: String): String

  @En("Asset {0} was updated")
  @Ja("{0} アセットを更新しました")
  @Ru("Актив {0} правлен")
  def successfullyUpdated(hostname: String): String

  @En("Delete")
  @Ja("削除")
  @Ru("Удалить")
  def delete: String
}

@C10NMessages
trait Tasks {
  @En("Tasks")
  @Ja("タスク")
  @Ru("Задачи")
  def title: String

  @En("Add Task")
  @Ja("タスク追加")
  @Ru("Добавить задачу")
  def addTask: String

  @En("New task was create")
  @Ja("タスクを作成しました")
  @Ru("Новая задача была добавлена")
  def successfullyAdded: String

  @En("Task was updated")
  @Ja("タスクを更新しました")
  @Ru("Задача правлена")
  def successfullyUpdated: String

  @En("Delete")
  @Ja("削除")
  @Ru("Удалить")
  def delete: String

  @En("Edit")
  @Ja("編集")
  @Ru("Править")
  def edit: String
}

@C10NMessages
trait ActivityView {
  @En("Recent Activity")
  @Ja("近況")
  @Ru("Журнал действий")
  def title: String

  @En("Undo")
  @Ja("元に戻す")
  @Ru("Отменить действие")
  def undo: String
}

@C10NMessages
trait Status {
  @En("Checking ...")
  @Ja("取得中 ...")
  @Ru("Идёт проверка ...")
  def checkingTitle: String

  @En("Pinging {0}")
  @Ja("{0} ping中")
  @Ru("Пинг-проверка {0}")
  def checking(ip: String): String

  @En("Online")
  @Ja("オンライン")
  @Ru("Онлайн")
  def okTitle: String

  @En("Successfully pinged {0}")
  @Ja("{0} ping成功")
  @Ru("Пинг-проверка {0} удалась")
  def ok(ip: String): String

  @En("Unreachable")
  @Ja("通信不可")
  @Ru("Хост недоступен")
  def unreachableTitle: String

  @En("Could not reach {0}")
  @Ja("{0}と通信できませんでした")
  @Ru("Попытка связи с {0} неудалась")
  def unreachable(ip: String): String

  @En("Network error")
  @Ja("通信エラー")
  @Ru("Ошибка связи")
  def errorTitle: String

  @En("Error trying connect to {0}: {1}")
  @Ja("{0}と通信中にエラーが発生しました: {1}")
  @Ru("Произошла ошибка при связи с {0}: {1}")
  def error(ip: String, error: String): String
}

@C10NMessages
trait Buttons {
  @En("New Asset")
  @Ja("アセット追加")
  @Ru("Добавить актив")
  def newAsset: String

  @En("Save")
  @Ja("保存")
  @Ru("Сохранить")
  def save: String

  @En("Cancel")
  @Ja("キャンセル")
  @Ru("Отменить")
  def cancel: String
}

@C10NMessages
trait Asset {
  @En("Assets")
  @Ja("アセット")
  @Ru("Активы")
  def title: String

  @En("Hostname")
  @Ja("ホスト名")
  @Ru("Имя хоста")
  def hostname: String

  @En("Name")
  @Ja("登録名")
  @Ru("Имя")
  def name: String

  @En("IP address")
  @Ja("IPアドレス")
  @Ru("IP адрес")
  def ip: String

  @En("Description")
  @Ja("概要")
  @Ru("Описание")
  def description: String

  @En("Admin")
  @Ja("管理者")
  @Ru("Администратор")
  def admin: String

  @En("Tags")
  @Ja("タグ")
  @Ru("Тэги")
  def tags: String

  @En("Status")
  @Ja("死活状況")
  @Ru("Статус")
  def status: String

  @En("Usage Status")
  @Ja("使用状況")
  @Ru("Статус")
  def usageStatus: String

  @En("Used")
  @Ja("使用中")
  @Ru("Используется")
  def used: String

  @En("Not used by anyone")
  @Ja("空いています")
  @Ru("Не используется")
  def available: String

  @En("{0} asset")
  @Ja("{0}アセット")
  @Ru("Актив {0}")
  def describe(hostname: String): String
}

@C10NMessages
trait Task {
  @En("User")
  @Ja("ユーザ")
  @Ru("Пользователь")
  def user: String

  @En("Description")
  @Ja("概要")
  @Ru("Описание")
  def description: String

  @En("Tags")
  @Ja("タグ")
  @Ru("Тэги")
  def tags: String

  @En("Icons")
  @Ja("アイコン")
  @Ru("Значёк")
  def icons: String

  @En("Task \"{1} ...\" by {0}")
  @Ja("{0}さんの\"{1} ...\"タスク")
  @Ru("Задача \"{1} ...\" пользователя {0}")
  def describe(user: String, briefDescription: String): String
}

@C10NMessages
trait Errors {
  @En("Not a valid IP address")
  @Ja("不正なIPアドレス")
  @Ru("IP адрес не верен")
  def invalidIP: String

  @En("<strong>Asset could not be saved: </strong> Please correct problems below and click 'Save'")
  @Ja("<strong>アセット保存に失敗しました: </strong> " +
    "入力内容を修正し「保存」ボタンをクリックしてください")
  @Ru("<strong>Актив не был сохранён: </strong> исправьте ошибки ниже и" +
    "попробуйте ещё раз.")
  def formValidationError: String
}

@C10NMessages
trait Activity {
  @En("{0} has been {1}")
  @Ja("{0}が{1}されました")
  @Ru("{0} был(а) {1}")
  def log(obj: String, action: String): String

  @En("added")
  @Ja("追加")
  @Ru("добавлен(а)")
  def added: String

  @En("addition")
  @Ja("追加")
  @Ru("добавление")
  def addition: String

  @En("modified")
  @Ja("編集")
  @Ru("изменен(а)")
  def modified: String

  @En("modification")
  @Ja("編集")
  @Ru("изменение")
  def modification: String

  @En("deleted")
  @Ja("削除")
  @Ru("удален(а)")
  def deleted: String

  @En("deletion")
  @Ja("削除")
  @Ru("удаление")
  def deletion: String

  @En("reverted to the state before {0}")
  @Ja("{0}のUNDO")
  @Ru("{0} отменено(а)")
  def undone(action: String): String

  @En("undo of {0}")
  @Ja("{0}のUNDO")
  @Ru("отменена {0}")
  def undo(action: String): String

  @En("asset")
  @Ja("アセット")
  @Ru("аткив")
  def assetEntry: String

  @En("task")
  @Ja("タスク")
  @Ru("задача")
  def assetTaskEntry: String
}