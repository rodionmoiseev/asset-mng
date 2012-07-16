package dao

/**
 *
 * @author rodion
 */

trait UIDProvider {
  def nextUID: Long
}
