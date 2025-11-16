package app.simple.inure.models

import android.app.AppOpsManager
import android.os.Build

/**
 * AppOps operation codes and monitoring configuration
 * Based on Rikka's AppOps implementation
 */
object AppOpsMonitor {

    /**
     * Operation groups for monitoring
     */
    data class OperationGroup(
        val nameResId: Int,
        val iconResId: Int,
        val operations: IntArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as OperationGroup

            if (nameResId != other.nameResId) return false
            if (iconResId != other.iconResId) return false
            if (!operations.contentEquals(other.operations)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = nameResId
            result = 31 * result + iconResId
            result = 31 * result + operations.contentHashCode()
            return result
        }
    }

    /**
     * Get AppOps operation code by name
     * Uses reflection to access hidden constants
     */
    @Suppress("SameParameterValue")
    private fun getOpCode(opName: String): Int {
        return try {
            val field = AppOpsManager::class.java.getField("OP_$opName")
            field.getInt(null)
        } catch (e: Exception) {
            -1
        }
    }

    // Location operations
    val OP_COARSE_LOCATION = getOpCode("COARSE_LOCATION")
    val OP_FINE_LOCATION = getOpCode("FINE_LOCATION")
    val OP_GPS = getOpCode("GPS")
    val OP_MONITOR_LOCATION = getOpCode("MONITOR_LOCATION")
    val OP_MONITOR_HIGH_POWER_LOCATION = getOpCode("MONITOR_HIGH_POWER_LOCATION")
    val OP_WIFI_SCAN = getOpCode("WIFI_SCAN")
    val OP_NEIGHBORING_CELLS = getOpCode("NEIGHBORING_CELLS")

    // Camera
    val OP_CAMERA = getOpCode("CAMERA")

    // Microphone
    val OP_RECORD_AUDIO = getOpCode("RECORD_AUDIO")

    // Clipboard (API 29+)
    val OP_READ_CLIPBOARD = getOpCode("READ_CLIPBOARD")
    val OP_WRITE_CLIPBOARD = getOpCode("WRITE_CLIPBOARD")

    // Phone
    val OP_READ_PHONE_STATE = getOpCode("READ_PHONE_STATE")
    val OP_CALL_PHONE = getOpCode("CALL_PHONE")

    // Contacts
    val OP_READ_CONTACTS = getOpCode("READ_CONTACTS")
    val OP_WRITE_CONTACTS = getOpCode("WRITE_CONTACTS")

    // SMS
    val OP_READ_SMS = getOpCode("READ_SMS")
    val OP_SEND_SMS = getOpCode("SEND_SMS")

    // Calendar
    val OP_READ_CALENDAR = getOpCode("READ_CALENDAR")
    val OP_WRITE_CALENDAR = getOpCode("WRITE_CALENDAR")

    // Call log
    val OP_READ_CALL_LOG = getOpCode("READ_CALL_LOG")
    val OP_WRITE_CALL_LOG = getOpCode("WRITE_CALL_LOG")

    // Sensors
    val OP_BODY_SENSORS = getOpCode("BODY_SENSORS")

    // Media location
    val OP_ACCESS_MEDIA_LOCATION = getOpCode("ACCESS_MEDIA_LOCATION")

    /**
     * All operations to monitor for active changes
     */
    fun getAllMonitoredOps(): IntArray {
        val ops = mutableListOf<Int>()

        // Location group
        if (OP_COARSE_LOCATION != -1) ops.add(OP_COARSE_LOCATION)
        if (OP_FINE_LOCATION != -1) ops.add(OP_FINE_LOCATION)
        if (OP_GPS != -1) ops.add(OP_GPS)
        if (OP_MONITOR_HIGH_POWER_LOCATION != -1) ops.add(OP_MONITOR_HIGH_POWER_LOCATION)
        if (OP_WIFI_SCAN != -1) ops.add(OP_WIFI_SCAN)
        if (OP_NEIGHBORING_CELLS != -1) ops.add(OP_NEIGHBORING_CELLS)

        // Camera
        if (OP_CAMERA != -1) ops.add(OP_CAMERA)

        // Microphone
        if (OP_RECORD_AUDIO != -1) ops.add(OP_RECORD_AUDIO)

        // Clipboard (API 29+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (OP_READ_CLIPBOARD != -1) ops.add(OP_READ_CLIPBOARD)
            if (OP_WRITE_CLIPBOARD != -1) ops.add(OP_WRITE_CLIPBOARD)
        }

        // Phone
        if (OP_READ_PHONE_STATE != -1) ops.add(OP_READ_PHONE_STATE)

        // Contacts
        if (OP_READ_CONTACTS != -1) ops.add(OP_READ_CONTACTS)

        // Sensors
        if (OP_BODY_SENSORS != -1) ops.add(OP_BODY_SENSORS)

        return ops.toIntArray()
    }

    /**
     * Map operation code to human-readable name
     */
    fun opToName(op: Int): String {
        return try {
            val method = AppOpsManager::class.java.getMethod("opToName", Int::class.javaPrimitiveType)
            method.invoke(null, op) as? String ?: "UNKNOWN_$op"
        } catch (e: Exception) {
            "UNKNOWN_$op"
        }
    }

    /**
     * Map operation code to permission name
     */
    fun opToPermission(op: Int): String? {
        return try {
            val method = AppOpsManager::class.java.getMethod("opToPermission", Int::class.javaPrimitiveType)
            method.invoke(null, op) as? String
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Check if operation is considered "sensitive" for monitoring
     */
    fun isSensitiveOp(op: Int): Boolean {
        return when (op) {
            OP_COARSE_LOCATION, OP_FINE_LOCATION, OP_GPS,
            OP_CAMERA, OP_RECORD_AUDIO,
            OP_READ_CLIPBOARD, OP_WRITE_CLIPBOARD,
            OP_READ_PHONE_STATE, OP_READ_CONTACTS,
            OP_BODY_SENSORS -> true
            else -> false
        }
    }
}
