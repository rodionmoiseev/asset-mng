$ ->
  #
  # Data bindings
  #
  $.getJSON '/dao/activity', (allData) ->
    activityList = new AM.ActivityList(allData)
    ko.applyBindings(activityList)