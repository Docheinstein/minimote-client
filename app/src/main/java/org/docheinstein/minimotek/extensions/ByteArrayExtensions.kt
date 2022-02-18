package org.docheinstein.minimotek.extensions

import kotlin.experimental.and

fun ByteArray.toBinaryString(pretty: Boolean = false): String {
    val sb = StringBuilder()
    for (b in this) {
        val block = java.lang.StringBuilder(Integer.toBinaryString(b.toInt() and 0xFF))
        while (block.length < 8)
            block.insert(0, "0")
        sb.append(block)
        if (pretty)
            sb.append( " | ")
    }
    return sb.toString()
}

//fun ByteArray.toBinaryString(pretty: Boolean = false) = toBlockString(pretty, 2, 8)
//fun ByteArray.toHexString(pretty: Boolean = false) = toBlockString(pretty, 16, 2)


// get 8 bytes as long
fun ByteArray.get64(offset: Int = 0): Long {
    return ((this[offset].toLong() and 0xFF) shl  56) or
        ((this[offset + 1].toLong() and 0xFF) shl  48) or
        ((this[offset + 2].toLong() and 0xFF) shl  40) or
        ((this[offset + 3].toLong() and 0xFF) shl  32) or
        ((this[offset + 4].toLong() and 0xFF) shl  24) or
        ((this[offset + 5].toLong() and 0xFF) shl  16) or
        ((this[offset + 6].toLong() and 0xFF) shl  8) or
        ((this[offset + 7].toLong() and 0xFF))
}

// set 8 bytes from a long
fun ByteArray.set64(value: Long, offset: Int = 0) {
    this[offset] =     ((value ushr 56) and 0xFFL).toByte()
    this[offset + 1] = ((value ushr 48) and 0xFFL).toByte()
    this[offset + 2] = ((value ushr 40) and 0xFFL).toByte()
    this[offset + 3] = ((value ushr 32) and 0xFFL).toByte()
    this[offset + 4] = ((value ushr 24) and 0xFFL).toByte()
    this[offset + 5] = ((value ushr 16) and 0xFFL).toByte()
    this[offset + 6] = ((value ushr 8) and 0xFFL).toByte()
    this[offset + 7] = ((value) and 0xFFL).toByte()
}

// set 4 bytes from an int
fun ByteArray.set32(value: Int, offset: Int = 0) {
    this[offset] =     ((value ushr 24) and 0xFF).toByte()
    this[offset + 1] = ((value ushr 16) and 0xFF).toByte()
    this[offset + 2] = ((value ushr 8) and 0xFF).toByte()
    this[offset + 3] = ((value)).toByte()
}

// set 2 bytes from an int
fun ByteArray.set16(value: Int, offset: Int = 0) {
    this[offset] =     ((value ushr 8) and 0xFF).toByte()
    this[offset + 1] = ((value) and 0xFF).toByte()
}
