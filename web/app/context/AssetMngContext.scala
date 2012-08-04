package context

import c10n.C10NMsgFactory
import i18n.Messages
import play.api.mvc.{AnyContent, Request, WrappedRequest}

/**
 *
 * @author rodion
 */

case class AssetMngContext(user: String, lang: String, c10nMsgFactory: C10NMsgFactory, request: Request[AnyContent])
  extends WrappedRequest(request) {
  lazy val m: Messages = c10nMsgFactory.get(classOf[Messages])
}
