package org.docheinstein.minimotek.minimote

import io.ktor.util.date.*
import org.docheinstein.minimotek.extensions.get8
import org.docheinstein.minimotek.extensions.set8
import org.docheinstein.minimotek.extensions.toBinaryString
import java.lang.Exception

/*
    | --- packet length --- | --- packet type --- | --- event time --- | ---- payload ---- |
    | ------- (1) --------- | ------ (1) -------- | ------- (6) -------| --- (variable) -- |
 */
class MinimotePacket(
    val packetLength: Int,
    val packetType: MinimotePacketType,
    val eventTime: Long,
    val payload: ByteArray
) {

    class InvalidPacketException(message: String) : Exception(message) {

    }

    companion object {
        const val HEADER_SIZE = 8

        fun fromData(data: ByteArray): MinimotePacket {
            if (data.size < HEADER_SIZE)
                throw InvalidPacketException("Invalid packet: must be at least 8 bytes, but it's ${data.size} bytes")

            val header: Long = data.get8()

            val packetLength = ((header ushr 56).toInt() and 0xFF)
            val packetTypeVal = ((header ushr 48).toInt() and 0xFF)
            val packetType = MinimotePacketType[packetTypeVal]
                ?: throw InvalidPacketException("Invalid packet: unknown packet type ${packetTypeVal.toString(16)}")

            val eventTime = header and 0xFFFFFFFFFFFFL

            val payload: ByteArray
            if (packetLength > HEADER_SIZE) {
                payload = ByteArray(packetLength - HEADER_SIZE)
                data.copyInto(payload, 0, HEADER_SIZE, packetLength)
            } else {
                payload = ByteArray(0)
            }

            return MinimotePacket(packetLength, packetType, eventTime, payload)
        }
    }

    constructor(packetLength: Int, packetType: MinimotePacketType, payload: ByteArray) : this(
        packetLength, packetType, getTimeMillis(), payload
    )

    constructor(packetType: MinimotePacketType) : this(
        HEADER_SIZE, packetType, getTimeMillis(), ByteArray(0)
    )


    fun toBytes(): ByteArray {
        val data = ByteArray(packetLength)

        val header: Long =
            ((packetLength and 0xFF).toLong() shl 56) or
            ((packetType.value and 0xFF).toLong() shl 48) or
            (eventTime and 0xFFFFFFFFFFFFL)

        data.set8(header)

        if (payload.isNotEmpty())
            payload.copyInto(data, HEADER_SIZE)

        return data
    }

    override fun toString(): String {
        return """
Packet length: $packetLength bytes
Packet type: $packetType
Event time: $eventTime
Payload: ${payload.toBinaryString(pretty = true)}
Dump: ${toBytes().toBinaryString(pretty = true)}
"""
    }
}