package assetstatus

import akka.actor.{ActorRef, Props, ActorSystem, Actor}
import akka.pattern.ask
import models.Asset
import akka.dispatch.Await
import akka.util.{Duration, Timeout}
import akka.util.duration.intToDurationInt
import java.util.concurrent.TimeUnit
import dao.DB
import java.util.Date

/**
 *
 * @author rodion
 */

class ActorBasedAssetStatusSystem(assetsDB: DB[Asset], statusChecker: AssetStatusChecker) extends AssetStatusSystem {
  /*
   * Interval in seconds between subsequent pings
   */
  val POLLING_INTERVAL_PROP = "assetmng.status.poll.interval"

  val system = ActorSystem("StatusPolling")
  val statusKeeper = system.actorOf(Props(new StatusKeeper()))
  val poller = system.actorOf(Props(new StatusPoller(statusKeeper, assetsDB, statusChecker)))

  system.scheduler.schedule(Duration(1, TimeUnit.SECONDS),
    Duration(java.lang.Long.getLong(POLLING_INTERVAL_PROP, 30), TimeUnit.SECONDS), poller, PingAll())

  def getStatus(asset: Asset) = {
    implicit val timeout = Timeout(2 seconds)
    Await.result(statusKeeper ? QueryStatus(asset), timeout.duration).asInstanceOf[AssetStatus]
  }
}

class StatusPoller(statusKeeper: ActorRef, assetsDB: DB[Asset], statusChecker: AssetStatusChecker) extends Actor {

  protected def receive = {
    case pingAll: PingAll =>
      statusKeeper ! Store(assetsDB.all.map(asset => (asset, statusChecker.checkStatus(asset))).toMap)
  }
}

class StatusKeeper extends Actor {
  private var results: Map[Asset, AssetStatus] = Map()

  protected def receive = {
    case store: Store => results = store.results
    case query: QueryStatus =>
      sender ! results.get(query.asset).getOrElse(AssetStatus(query.asset, "checking", new Date))
  }
}

case class PingAll()

case class QueryStatus(asset: Asset)

case class Store(results: Map[Asset, AssetStatus])