package controllers

import java.net.{InetAddress, Inet6Address, Inet4Address}
import sun.net.util.IPAddressUtil


/**
 *
 * @author rodion
 */

object IPUtils {
  /**
   * Tests if the given string is a valid IPv4 or IPv6 address.
   * @param ip string to test
   * @return
   */
  def isIPAddress(ip: String): Boolean = {
    ip != null && (
      IPAddressUtil.isIPv4LiteralAddress(ip) ||
      IPAddressUtil.isIPv6LiteralAddress(ip)
    )
  }
}
