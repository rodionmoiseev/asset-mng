class AM.submitForm
  constructor: (lang) ->
    $('#login-form-lang').val(lang)
    $('#login-form').submit()

$ ->
  $('#login-form-name').focus()