window.AM = {}

class AM.Asset
  constructor: (asset) ->
    @hostname = ko.observable(asset.hostname)
    @ip = ko.observable(asset.ip)
    @description = ko.observable(asset.description)
    @admin = ko.observable(asset.admin)
    @tags = ko.observable(asset.tags)

class AM.AssetList
  constructor: (assets) ->
    @assets = ko.observableArray([])
    @assets($.map assets, (asset) -> new AM.Asset(asset))

  addAsset: (asset) ->
    @assets.unshift new AM.Asset(asset)

class AM.AssetTask
  constructor: (task) ->
    @id = ko.observable(task.id)
    @user = ko.observable(task.user)
    @desc = ko.observable(task.desc)
    @date = ko.observable(task.date)

  showControls: (item, event) ->
    $(event.target).find(".asset-controls").animate({opacity: 0.6}, 100)

  hideControls: (item, event) ->
    $(event.target).find(".asset-controls").animate({opacity: 0.2}, 100)

class AM.AssetTaskGroup
  constructor: (taskGroup) ->
    @asset = ko.observable(new AM.Asset(taskGroup.asset))
    @tasks = ko.observableArray([])
    @tasks($.map taskGroup.tasks, (task) -> new AM.AssetTask(task))

class AM.AssetTaskGroupList
  constructor: (taskGroups) ->
    @taskGroups = ko.observableArray([])
    @taskGroups($.map taskGroups, (taskGroup) -> new AM.AssetTaskGroup(taskGroup))

  addTask: (task) ->
    ko.utils.arrayForEach @taskGroups, (taskGroup) ->
      if taskGroup.asset().hostname = task.hostname then taskGroup.tasks.unshift new AM.AssetTask(task)