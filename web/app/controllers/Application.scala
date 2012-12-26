package controllers

import play.api.mvc._
import play.api.data.Form
import play.api.data.Forms._
import c10n.{C10NMsgFactory, LocaleProvider, C10NConfigBase, C10N}
import c10n.annotations.{En, DefaultC10NAnnotations}
import java.util.Locale
import context.AssetMngContext
import i18n.Messages

object Application extends Controller {
  val HOSTNAME = System.getProperty("assetmng.hostname", "localhost")

  case class LoginForm(name: String, lang: String)

  val loginForm = Form(mapping(
    "name" -> nonEmptyText,
    "lang" -> nonEmptyText
  )(LoginForm.apply)(LoginForm.unapply))

  def tasks = AssetMngAction {
    implicit ctx =>
      Ok(views.html.tasks())
  }

  def assets = AssetMngAction {
    implicit ctx =>
      Ok(views.html.assets())
  }

  def activity = AssetMngAction {
    implicit ctx =>
      Ok(views.html.activity())
  }

  def index = AssetMngAction {
    implicit ctx =>
      Redirect(routes.Application.assets())
  }

  def login = Action {
    implicit request =>
      loginForm.bindFromRequest.fold(
        noLoginSupplied => {
          Ok(views.html.login(C10N.get(classOf[Messages], getLocale(request))))
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
          request.session +("user", form.name) +("c10n-lang", form.lang)
        )
      )
  }

  def logout = Action {
    Redirect(routes.Application.index()).withNewSession
  }

  def AssetMngAction(f: AssetMngContext => PlainResult) = {
    Action {
      request => {
        val lang = getLang(request)
        request.session.get("user").map(user =>
          f(AssetMngContext(user, lang, request)).withSession(
            request.session
              +("user", user)
              +("c10n-lang", lang)
          )
        ).getOrElse {
          Redirect(routes.Application.login()).withSession(
            request.session +("logst_referer", request.uri)
          )
        }
      }
    }
  }

  def getLocale(request: Request[AnyContent]): Locale = new Locale(getLang(request))

  private def getLang(request: Request[AnyContent]): String =
    request.session.get("c10n-lang").getOrElse(request.queryString.get("lang").getOrElse(Seq("en")).head)
}