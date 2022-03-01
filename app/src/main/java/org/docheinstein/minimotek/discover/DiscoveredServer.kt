package org.docheinstein.minimotek.discover

/**
 * Represents a server discovered through the broadcast mechanism used in [Discoverer].
 */
data class DiscoveredServer(
    val address: String,
    val port: Int,
    val hostname: String,
) {
    override fun toString(): String {
        return "(address=$address, port=$port, hostname=$hostname)"
    }
}