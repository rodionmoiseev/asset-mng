window.AM = {}

class AM.Utils
  contains: (str, part) ->
    str.toLowerCase().indexOf(part) != -1

  listContains: (list, part) ->
    for item in list
      if @contains(item, part) then return true
    false

  #
  # Merge two maps into one.
  # Key in the second argument will override
  # the ones in the first.
  merge: (a, b) ->
    res = new Object
    for k,v of a
      res[k] = v
    for k,v of b
      res[k] = v
    res

  #
  # Add typeahead (auto-completion) functionality to
  # the given DOM element(s), with custom settings object
  addTypeahead: (domElements, typeAheadSettings) =>
    typeaheadUpdater = (item) ->
      res = @query.split(',')[0..-2]
      res.push item
      res = (x.trim() for x in res)
      res.join ", "
    typeaheadMatcher = (item) -> ~item.toLowerCase().indexOf(@query.toLowerCase().split(',').pop().trim())

    defaultSettings =
      updater: typeaheadUpdater
      matcher: typeaheadMatcher
    domElements.typeahead(@merge defaultSettings, typeAheadSettings)


class AM.Asset
  constructor: (asset) ->
    @id = asset.id
    @hostname = ko.observable('')
    @ip = ko.observable('')
    @description = ko.observable('')
    @admin = ko.observable('')
    @tags = ko.observableArray([])
    @update asset
    @usageStatus = ko.observable(asset.usageStatus)
    @status_message = asset.status.message + '<br><small>' + asset.status.lastChecked + '</small>'
    @status_title = asset.status.title
    @status_icon = switch asset.status.status
                      when "ok" then "icon-ok-sign"
                      when "unreachable" then "icon-exclamation-sign"
                      when "checking" then "icon-time"
                      else "icon-question-sign"

  update: (asset) ->
    @hostname(asset.hostname)
    @ip(asset.ip)
    @description(asset.description)
    @admin(asset.admin)
    @tags(asset.tags)

class AM.AssetList extends AM.Utils
  constructor: (assets, assetForm) ->
    @filter = ko.observable('')
    @assets = ko.observableArray([])
    @assets($.map assets, (asset) -> new AM.Asset(asset))
    @assetForm = assetForm
    @filteredAssets = ko.computed =>
      lfilter = @filter().toLowerCase()
      if !lfilter then @assets() else ko.utils.arrayFilter @assets(), (asset) =>
        (@contains asset.hostname(), lfilter) or
          (@contains asset.ip(), lfilter) or
          (@contains asset.admin(), lfilter) or
          (@contains asset.description(), lfilter) or
          (@listContains asset.tags(), lfilter)

  addAsset: (newAsset) ->
    match = ko.utils.arrayFirst @assets(), (asset) -> asset.id is newAsset.id
    if match
      match.update newAsset
    else
      @assets.unshift new AM.Asset(newAsset)

  removeAsset: (asset) =>
    $.ajax
       url: '/dao/assets/delete/' + asset.id
       type: 'DELETE'
       success: (response) =>
         $('.delete-asset').tooltip('hide')
         @assets.remove(asset)
       error: (jqXHR) =>
         window.console.log(jqXHR.responseText)

  editAsset: (asset) =>
    @assetForm.copyFromAsset asset
    @assetForm.show()

  decorate: ->
    $('.delete-asset').tooltip
      placement: 'right'
    $('.edit-asset').tooltip
      placement: 'right'
    $('.asset-status').popover
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

class AM.AssetTaskGroup extends AM.Utils
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
    (@contains task.description(), filter) or
      (@contains task.user(), filter) or
      (@listContains task.tags, filter) or
      (@listContains task.icons, filter)

  removeTask: (task) =>
    $.ajax
      url: '/dao/tasks/delete/' + task.id
      type: 'DELETE'
      success: (response) =>
        $('.delete-task').tooltip('hide')
        @tasks.remove(task)
      error: (jqXHR) =>
        window.console.log(jqXHR.responseText)

  editTask: (task, event) =>
    parent = $(event.target).parent().parent().children('.asset-task-description')
    view = parent.children('span')
    editor = parent.children('.edit-asset-task-description')
    view.hide('fast')
    editor.show('fast')
    editor.children('a').click ->
      $.ajax
        url: '/dao/tasks/update'
        type: 'POST'
        data: ko.toJSON(task, ['id', 'asset_id', 'user', 'description', 'date', 'tags', 'icons'])
        contentType: 'application/json'
        success: (response) =>
          view.show('fast')
          editor.hide('fast')
        error: (jqXHR) =>
          window.console.log(jqXHR.responseText)

  decorate: ->
    $('.delete-task').tooltip()
    $('.edit-task').tooltip()

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

class AM.ActivityList extends AM.Utils
  constructor: (activities) ->
    @filter = ko.observable('')
    @activities = ko.observableArray([])
    @activities($.map activities, (activity) -> new AM.Activity(activity))
    @filteredActivities = ko.computed =>
      lfilter = @filter().toLowerCase()
      if !lfilter then @activities() else ko.utils.arrayFilter @activities(), (activity) =>
        (@contains activity.user, lfilter) or
          (@contains activity.action, lfilter) or
          (@contains activity.obj, lfilter)

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