package models

/**
 *
 * @author rodion
 */

case class AssetState(asset: Asset, status: String, var tasks: List[AssetTask])