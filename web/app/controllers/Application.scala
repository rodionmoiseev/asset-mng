package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._

object Application extends Controller {

  case class LoginForm(name: String)

  val loginForm = Form(mapping(
    "name" -> nonEmptyText
  )(LoginForm.apply)(LoginForm.unapply))

  def tasks = AssetMngAction {
    (user, request) =>
      Ok(views.html.tasks(user))
  }

  def assets = AssetMngAction {
    (user, request) =>
      Ok(views.html.assets(user))
  }

  def activity = AssetMngAction {
    (user, request) =>
      Ok(views.html.activity(user))
  }

  def index = AssetMngAction {
    (user, request) =>
      Redirect(routes.Application.assets())
  }

  def login = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        noLoginSupplied => {
          Ok(views.html.login())
        },
        form => {
          request.session.get("logst_referer") match {
            case Some(url) => {
              if (url.endsWith("login")) {
                Redirect(routes.Application.index())
              } else {
                Redirect(url)
              }
            }
            case None => Redirect(routes.Application.index())
          }
        }.withSession(
          request.session +("user", form.name)
        )
      )
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession
  }

  def AssetMngAction(f: (String, Request[AnyContent]) => PlainResult) = {
    Action {
      request =>
        request.session.get("user").map(user => f(user, request).withSession(
          request.session +("user", user))
        ).getOrElse {
          Redirect(routes.Application.login()).withSession(
            request.session +("logst_referer", request.uri)
          )
        }
    }
  }
}