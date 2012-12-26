package context

import c10n.{C10N, C10NMsgFactory}
import i18n.Messages
import play.api.mvc.{AnyContent, Request, WrappedRequest}
import java.util.Locale

/**
 *
 * @author rodion
 */

case class AssetMngContext(user: String, lang: String, request: Request[AnyContent])
  extends WrappedRequest(request) {
  lazy val m: Messages = C10N.get(classOf[Messages], new Locale(lang))
}
