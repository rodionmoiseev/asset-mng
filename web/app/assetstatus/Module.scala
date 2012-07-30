package assetstatus

/**
 *
 * @author rodion
 */

object Module {
  val statusChecker: AssetStatusChecker = new PingAssetStatusChecker
  implicit val assetStatusSystem: AssetStatusSystem = new ActorBasedAssetStatusSystem(dao.Module.assetsDB, statusChecker)
}
