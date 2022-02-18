package org.docheinstein.minimotek.extensions

import io.ktor.utils.io.*

suspend fun ByteWriteChannel.writeFully(src: ByteArray) {
    writeFully(src, 0, src.size)
}
