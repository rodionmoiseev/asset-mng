class NewAssetForm
  constructor: (assetList) ->
    @hostname = ko.observable('')
    @ip = ko.observable('')
    @description = ko.observable('')
    @admin = ko.observable('')
    @tags = ko.observable('')
    @fields = ['hostname', 'ip', 'description', 'admin', 'tags']
    @errors = {}
    @errors[name] = ko.observable('') for name in @fields
    @assetList = assetList

  fillErrors: (e) ->
    for name, value of e
      @errors[name](value)

  clear: ->
    @[field]('') for field in @fields
    @errors[name]('') for name in @fields

  save: ->
    $.ajax
      url: '/dao/assets/add'
      type: 'POST'
      data: ko.toJSON(@)
      contentType: 'application/json'
      success: (response) =>
        r = JSON.parse(response)
        @assetList.addAsset r.asset
        alrt = $('#alert-add-asset')
        alrt.children('span').text r.status
        alrt.show()
        @clear()
      error: (jqXHR) =>
        @fillErrors(JSON.parse(jqXHR.responseText))
#gen = window.open('', 'Error')
#gen.document.write(jqXHR.responseText)
#gen.document.close()

$ ->
  #
  # Data bindings
  #
  $.getJSON '/dao/assets', (allData) ->
    assetList = new AM.AssetList(allData)
    ko.applyBindings(assetList, $('#assets-list')[0])
    ko.applyBindings(new NewAssetForm(assetList), $('#form-add-asset')[0])

  #
  # New Asset Form click handlers
  #
  $('#alert-add-asset').children('.close').click => $('#alert-add-asset').hide()
  $('#link-add-asset').click -> $('#form-add-asset').show 'fast'
  $('#btn-cancel-add-asset').click -> $('#form-add-asset').hide 'fast'

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