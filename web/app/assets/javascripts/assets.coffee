class NewAssetForm
  constructor: ->
    @id = -1
    @hostname = ko.observable('')
    @ip = ko.observable('')
    @description = ko.observable('')
    @admin = ko.observable('')
    @tags = ko.observable('')
    @fields = ['hostname', 'ip', 'description', 'admin', 'tags']
    @errors = {}
    @errors[name] = ko.observable('') for name in @fields

  setAssetList: (assetList) ->
    @assetList = assetList

  copyFromAsset: (asset) ->
    @clear()
    @id = asset.id
    @hostname(asset.hostname())
    @ip(asset.ip())
    @description(asset.description())
    @admin(asset.admin())
    @tags(asset.tags().join(', '))

  fillErrors: (e) ->
    for name, value of e
      @errors[name](value)

  clear: ->
    @id = -1
    @[field]('') for field in @fields
    @errors[name]('') for name in @fields

  show: ->
    $('#form-add-asset').modal('show')

  hide: ->
    $('#form-add-asset').modal('hide')

  save: ->
    postUrl = if @id == -1 then '/dao/assets/add' else '/dao/assets/update'
    $.ajax
      url: postUrl
      type: 'POST'
      data: ko.toJSON(@, ['id', 'hostname', 'ip', 'description', 'admin', 'tags'])
      contentType: 'application/json'
      success: (response) =>
        r = JSON.parse(response)
        @assetList.addAsset r.asset
        alrt = $('#alert-add-asset')
        alrt.children('span').text r.status
        alrt.show()
        @clear()
        @hide()
      error: (jqXHR) =>
        @fillErrors(JSON.parse(jqXHR.responseText))
        #gen = window.open('', 'Error')
        #gen.document.write(jqXHR.responseText)
        #gen.document.close()

$ ->
  assetForm = new NewAssetForm
  #
  # Data bindings
  #
  $.getJSON '/dao/assets', (allData) ->
    ko.applyBindings(assetForm, $('#form-add-asset')[0])
    assetList = new AM.AssetList(allData, assetForm)
    assetForm.setAssetList assetList
    ko.applyBindings(assetList, $('#assets-list')[0])

  #
  # New Asset Form click handlers
  #
  $('#alert-add-asset').children('.close').click => $('#alert-add-asset').hide()
  $('#link-add-asset').click ->
    assetForm.clear()
    assetForm.show()

  #
  # Enable tablesorter.js
  #
  $('#myTable').tablesorter
    headers:
      6:
        sorter: false
  ko.bindingHandlers.triggerUpdate =
    update: (element, valueAccessor) ->
      ko.utils.unwrapObservable valueAccessor()
      $(element).trigger 'update'

  #
  # Initialise typeahead (auto-completion)
  #
  $.getJSON "/dao/tags", (tags) =>
    utils = new AM.Utils
    utils.addTypeahead $('#tags'), source: tags

  #
  # binding for 'id' attribute
  #
  ko.bindingHandlers.id =
    init: (element, valueAccessor, allBindingsAccessor, viewModel) =>
    update: (element, valueAccessor, allBindingsAccessor, viewModel) => $(element).attr 'id', valueAccessor()