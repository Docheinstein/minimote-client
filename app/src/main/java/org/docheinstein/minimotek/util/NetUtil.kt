package org.docheinstein.minimotek.util

// https://stackoverflow.com/questions/5284147/validating-ipv4-addresses-with-regexp
private val IP_REGEX = Regex("^(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])$")

object NetUtils {
    fun isValidIPv4(ip: String): Boolean {
        return IP_REGEX.matches(ip)
    }

    fun isValidPort(port: String): Boolean {
        return try {
            val portInt = port.toInt()
            portInt in 0..65535
        } catch (e: NumberFormatException) {
            false
        }
    }
}