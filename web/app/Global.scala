import c10n.annotations.{En, DefaultC10NAnnotations}
import c10n.{C10NConfigBase, C10N}
import play.api._
import play.api.Application
import play.api.Play.current

/**
 *
 * @author rodion
 */

object Global extends GlobalSettings {
  override def onStart(app: Application) {
    if (Play.isDev) {
      C10N.setProxyClassloader(Play.classloader)
    }
  }
}