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
    @filter = ko.observable('')
    @assets = ko.observableArray([])
    @assets($.map assets, (asset) -> new AM.Asset(asset))
    @filteredAssets = ko.computed =>
      lfilter = @filter().toLowerCase()
      if !lfilter then @assets() else ko.utils.arrayFilter @assets(), (asset) ->
        contains = (str, part) -> str.indexOf(part) != -1
        (contains asset.hostname().toLowerCase(), lfilter) or
          (contains asset.ip().toLowerCase(), lfilter) or
          (contains asset.admin().toLowerCase(), lfilter) or
          (contains asset.description().toLowerCase(), lfilter)

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

  decorate: ->
    $('.delete-asset').tooltip
      placement: 'right'

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
  constructor: (parent, taskGroup) ->
    @parent = parent
    @asset = ko.observable(new AM.Asset(taskGroup.asset))
    @tasks = ko.observableArray([])
    @tasks($.map taskGroup.tasks, (task) -> new AM.AssetTask(task))
    @filteredTasks = ko.computed =>
      lfilter = @parent.filter().toLowerCase()
      if !lfilter then @tasks() else ko.utils.arrayFilter @tasks(), (task) => @taskMatches task, lfilter

  containsTaskMatching: (filter) =>
    found = ko.utils.arrayFirst @tasks(), (task) => @taskMatches task, filter
    found?

  taskMatches: (task, filter) ->
    contains = (str, part) -> str.indexOf(part) != -1
    (contains task.description().toLowerCase(), filter) or
      (contains task.user().toLowerCase(), filter)

  removeTask: (task) =>
    $.ajax
      url: '/dao/tasks/delete/' + task.id
      type: 'DELETE'
      success: (response) =>
        @tasks.remove(task)
      error: (jqXHR) =>
        window.console.log(jqXHR.responseText)

  decorate: ->
    $('.delete-task').tooltip()

class AM.AssetTaskGroupList
  constructor: (taskGroups) ->
    @filter = ko.observable('')
    @taskGroups = ko.observableArray([])
    @taskGroups($.map taskGroups, (taskGroup) => new AM.AssetTaskGroup(@, taskGroup))
    @filteredTaskGroups = ko.computed =>
      lfilter = @filter().toLowerCase()
      if !lfilter then @taskGroups() else ko.utils.arrayFilter @taskGroups(), (taskGroup) ->
        taskGroup.containsTaskMatching(lfilter)

  addTask: (task) ->
    ko.utils.arrayForEach @taskGroups(), (taskGroup) ->
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
    @filter = ko.observable('')
    @activities = ko.observableArray([])
    @activities($.map activities, (activity) -> new AM.Activity(activity))
    @filteredActivities = ko.computed =>
      lfilter = @filter().toLowerCase()
      if !lfilter then @activities() else ko.utils.arrayFilter @activities(), (activity) ->
        contains = (str, part) -> str.indexOf(part) != -1
        (contains activity.user.toLowerCase(), lfilter) or
          (contains activity.action.toLowerCase(), lfilter) or
          (contains activity.obj.toLowerCase(), lfilter)

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

  decorate: ->
    $('.undo-activity').tooltip()