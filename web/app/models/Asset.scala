package models

import i18n.Messages
import org.codehaus.jackson.annotate.JsonTypeInfo
import org.codehaus.jackson.annotate.JsonTypeInfo.Id

/**
 *
 * @author rodion
 */

case class Asset(
                  id: Long,
                  hostname: String,
                  ip: String,
                  description: String,
                  admin: String,
                  parent_id: Option[Long]) extends HistoryObject with Persistent[Asset]{
  def withId(id: Long) = copy(id)
  def describe(implicit m: Messages) = m.asset.describe(hostname)
}
