window.AM = {}

class AM.Asset
  constructor: (asset) ->
    @id = asset.id
    @hostname = ko.observable(asset.hostname)
    @ip = ko.observable(asset.ip)
    @description = ko.observable(asset.description)
    @admin = ko.observable(asset.admin)

class AM.AssetList
  constructor: (assets) ->
    @assets = ko.observableArray([])
    @assets($.map assets, (asset) -> new AM.Asset(asset))

  addAsset: (asset) ->
    @assets.unshift new AM.Asset(asset)

  removeAsset: (asset) =>
    $.ajax
       url: '/dao/assets/delete/' + asset.id
       type: 'DELETE'
       success: (response) =>
         @assets.remove(asset)
       error: (jqXHR) =>
         window.console.log(jqXHR.responseText)

class AM.AssetTask
  constructor: (task) ->
    @id = task.id
    @asset_id = task.asset_id
    @user = ko.observable(task.user)
    @description = ko.observable(task.description)
    @date = ko.observable(task.date)
    @tags = task.tags
    @icons = task.icons

  showControls: (item, event) ->
    $(event.target).find(".asset-controls").animate({opacity: 0.6}, 100)

  hideControls: (item, event) ->
    $(event.target).find(".asset-controls").animate({opacity: 0.2}, 100)

class AM.AssetTaskGroup
  constructor: (taskGroup) ->
    @asset = ko.observable(new AM.Asset(taskGroup.asset))
    @tasks = ko.observableArray([])
    @tasks($.map taskGroup.tasks, (task) -> new AM.AssetTask(task))

  removeTask: (task) =>
    $.ajax
      url: '/dao/tasks/delete/' + task.id
      type: 'DELETE'
      success: (response) =>
        @tasks.remove(task)
      error: (jqXHR) =>
        window.console.log(jqXHR.responseText)

class AM.AssetTaskGroupList
  constructor: (taskGroups) ->
    @taskGroups = ko.observableArray([])
    @taskGroups($.map taskGroups, (taskGroup) -> new AM.AssetTaskGroup(taskGroup))

  addTask: (task) ->
    window.console.log("addTask(" + task + ")")
    ko.utils.arrayForEach @taskGroups(), (taskGroup) ->
      window.console.log("match taskGroup.asset.id=" + taskGroup.asset().id + " with task.asset_id=" + task.asset_id)
      if taskGroup.asset().id == task.asset_id then taskGroup.tasks.unshift new AM.AssetTask(task)

class AM.Activity
  constructor: (activity) ->
    @id = activity.id
    @user = activity.user
    @action = activity.action
    @obj = activity.obj
    @date = activity.date
    @canUndo = activity.canUndo

class AM.ActivityList
  constructor: (activities) ->
    @activities = ko.observableArray([])
    @activities($.map activities, (activity) -> new AM.Activity(activity))

  undo: (activity) =>
    $.ajax
      url: '/dao/activity/undo/' + activity.id
      type: 'POST'
      success: (response) =>
        r = JSON.parse(response)
        @addActivity r.activity
      error: (jqXHR) =>
        window.console.log(jqXHR.responseText)

  addActivity: (activity) ->
    @activities.unshift new AM.Activity(activity)