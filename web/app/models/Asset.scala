package models

/**
 *
 * @author rodion
 */

case class Asset(hostname: String, ip: String, description: String, admin: String, tags: List[String])
