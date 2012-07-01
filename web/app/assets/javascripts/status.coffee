class NewTaskForm
  constructor: (taskList, allAssets) ->
    @assets = allAssets.assets
    @asset = ko.observable()
    @hostname = ko.computed(=> @asset()?.hostname)
    @description = ko.observable('')
    @user = ko.observable('')
    @tags = ko.observable('')
    @fields = ['asset', 'description', 'user', 'tags']
    @errors = {}
    @errors[name] = ko.observable('') for name in @fields
    @taskList = taskList

  fillErrors: (e) ->
    for name, value of e
      @errors[name](value)

  clear: ->
    @[field]('') for field in @fields
    @errors[name]('') for name in @fields

  save: ->
    $.ajax
      url: '/dao/tasks/add'
      type: 'POST'
      data: ko.toJSON(@)
      contentType: 'application/json'
      success: (response) =>
        r = JSON.parse(response)
        @taskList.addTask r.task
        alrt = $('#alert-add-task')
        alrt.children('span').text r.status
        alrt.show()
        @clear()
      error: (jqXHR) =>
        @fillErrors(JSON.parse(jqXHR.responseText))

$ ->
  $.getJSON "/dao/tasks/groupedByAsset", (allData) ->
    taskGroupList = new AM.AssetTaskGroupList(allData)
    ko.applyBindings(taskGroupList, $('#tasks-list')[0])
    $.getJSON '/dao/assets', (allAssets) ->
      assetList = new AM.AssetList(allAssets)
      ko.applyBindings(new NewTaskForm(taskGroupList, assetList), $('#form-add-task')[0])

  #
  # New Task Form click handlers
  #
  $('#alert-add-task').children('.close').click => $('#alert-add-task').hide()
  $('#link-add-task').click -> $('#form-add-task').show 'fast'
  $('#btn-cancel-add-task').click -> $('#form-add-task').hide 'fast'