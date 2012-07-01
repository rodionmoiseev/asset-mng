package controllers

import play.api.data._
import play.api.data.Forms._
import play.api.mvc._
import models.Asset
import i18n.Messages
import play.api.Routes

object Application extends Controller {
  def status = Action{
    Ok(views.html.status())
  }

  def assets = Action {
    Ok(views.html.assets())
  }

  def index = Action {
    Redirect(routes.Application.assets())
  }
}