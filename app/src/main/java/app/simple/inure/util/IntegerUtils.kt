// SPDX-License-Identifier: GPL-3.0-or-later
package app.simple.inure.util

import java.nio.ByteBuffer

object IntegerUtils {
    fun getUInt8(buffer: ByteBuffer): Int {
        return buffer.get().toInt() and 0xff
    }

    @JvmStatic
    fun getUInt16(buffer: ByteBuffer): Int {
        return buffer.getShort().toInt() and 0xffff
    }

    fun getUInt32(buffer: ByteBuffer): Long {
        return buffer.getInt().toLong() and 0xffffffffL
    }

    fun getUInt32(buffer: ByteBuffer, position: Int): Long {
        return buffer.getInt(position).toLong() and 0xffffffffL
    }
}