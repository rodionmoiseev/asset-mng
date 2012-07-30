package assetstatus

import models.Asset
import java.util.Date

/**
 *
 * @author rodion
 */

trait AssetStatusSystem {
  def getStatus(asset: Asset): AssetStatus
}

case class AssetStatus(asset: Asset, status: String, lastChecked: Date, error: String = ""){
  def lastCheckedStr = "%1$tm/%1$td %1$tH:%1$tM:%1$tS".format(lastChecked)
}
