class NewTaskForm
  constructor: (taskList, allAssets) ->
    @assets = allAssets.assets
    @asset = ko.observable()
    @asset_id = ko.computed(=> @asset()?.id)
    @description = ko.observable('')
    @user = ko.observable('')
    @tags = ko.observable('')
    @icons = ko.observable('')
    @fields = ['asset', 'description', 'user', 'tags', 'icons']
    @errors = {}
    @errors[name] = ko.observable('') for name in @fields
    @taskList = taskList

  fillErrors: (e) ->
    for name, value of e
      @errors[name]?(value)

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
        @taskList.addTask(r.task)
        alrt = $('#alert-add-task')
        alrt.children('span').text r.status
        alrt.show()
        @clear()
      error: (jqXHR) =>
        window.console.log(jqXHR.responseText)
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

  a = $("#icons").typeahead
    source: window.twitter_bootstrap_icons
    updater: (item) ->
      res = @query.split(',')[0..-2]
      res.push item
      res = (x.trim() for x in res)
      res.join ", "
    matcher: (item) -> ~item.toLowerCase().indexOf(@query.toLowerCase().split(',').pop().trim())
    highlighter: (item) -> '<i class="icon-' + item + '"></i> ' + item