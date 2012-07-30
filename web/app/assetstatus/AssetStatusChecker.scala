package assetstatus

import models.Asset
import java.net.InetAddress
import java.io.IOException
import java.util.Date

/**
 *
 * @author rodion
 */

trait AssetStatusChecker {
  def checkStatus(asset: Asset): AssetStatus
}

class PingAssetStatusChecker extends AssetStatusChecker {
  val PING_TIMEOUT_PROP = "assetmng.status.poll.pingTimeout"

  def checkStatus(asset: Asset) = {
    val stat = try {
      InetAddress.getByName(asset.ip).isReachable(Integer.getInteger(PING_TIMEOUT_PROP, 2000)) match {
        case true => ("ok", "")
        case _ => ("unreachable", "")
      }
    } catch {
      case networkError: IOException => ("error", networkError.getMessage)
    }
    AssetStatus(asset, stat._1, new Date, error = stat._2)
  }
}