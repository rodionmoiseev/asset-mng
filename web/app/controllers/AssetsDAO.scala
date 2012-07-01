package controllers

import models.{Used, Free, Asset}


/**
 *
 * @author rodion
 */

trait AssetsDAO {
  def save(asset: Asset)
  def list(): List[Asset]
}

object SimpleAssetsDAO{
  val me = new SimpleAssetsDAO
  def get = me
}

class SimpleAssetsDAO extends AssetsDAO{
  var sampleData : List[Asset] = List(
    Asset("logst21", "127.0.0.1", "dev server\nmulti\nline", "rodion", List("tag1")),
    Asset("logst22", "127.0.0.2", "dev server", "konagaya", List("tag1", "tag2"))
  )

  override def save(asset: Asset) = sampleData = asset :: sampleData
  override def list() = sampleData
}