package org.docheinstein.minimotek.data.discover


data class DiscoveredServer(
    val address: String,
    val port: Int,
    val hostname: String,
) {
    override fun toString(): String {
        return "(address=$address, port=$port, hostname=$hostname)"
    }
}