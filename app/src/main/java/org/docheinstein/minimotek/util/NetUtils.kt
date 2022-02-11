package org.docheinstein.minimotek.util

object NetUtils {
    fun isValidIPv4(ip: String): Boolean {
        if (ip.isEmpty() || ip.length > 15)
            return false

        val quartets = ip.split(Regex("\\."))

        if (quartets.size != 4)
            return false

        for (quartet in quartets) {
            try {
                val q = quartet.toInt()
                if (q < 0 || q > 255)
                    return false
            } catch (e: NumberFormatException) {
                return false
            }
        }

        return true
    }

    fun isValidPort(port: Int): Boolean {
        return port in 0..65535
    }
}