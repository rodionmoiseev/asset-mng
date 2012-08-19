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
      data: ko.toJSON(@, ['asset_id', 'description', 'user', 'tags', 'icons'])
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

  #
  # Task controls fade in/out effect
  #$(@).find('.asset-task-controls').animate { opacity: 1.0 }, 'fast',
  $('.asset-task-controls').live 'mouseenter', ->
    $(@).css { opacity: 1.0 }
  $('.asset-task-controls').live 'mouseleave', ->
    $(@).css { opacity: 0.3 }

  #
  # Initialise typeahead (auto-completion)
  #
  utils = new AM.Utils
  $.getJSON "/dao/tags", (tags) =>
    utils.addTypeahead $('#tags'), source: tags

  utils.addTypeahead $("#icons"),
    source: window.twitter_bootstrap_icons
    highlighter: (item) -> '<i class="icon-' + item + '"></i> ' + item