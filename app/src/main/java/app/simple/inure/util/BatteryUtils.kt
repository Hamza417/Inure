package app.simple.inure.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import app.simple.inure.R
import app.simple.inure.math.Extensions.round2

object BatteryUtils {

    /**
     * @return [Intent] with battery information
     */
    fun getBatteryStatusIntent(context: Context): Intent? {
        val iFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        return context.registerReceiver(null, iFilter)
    }

    /**
     * @return battery capacity from private API. In case of error it will return -1.
     */
    @SuppressLint("PrivateApi")
    fun getBatteryCapacity(context: Context): Double {
        var capacity = -1.0
        try {
            val powerProfile = Class.forName("com.android.internal.os.PowerProfile")
                .getConstructor(Context::class.java).newInstance(context)
            capacity = Class
                .forName("com.android.internal.os.PowerProfile")
                .getMethod("getAveragePower", String::class.java)
                .invoke(powerProfile, "battery.capacity") as Double
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return capacity
    }

    /**
     * @return battery health status as a string
     */
    fun getBatteryHealthStatus(healthInt: Int, context: Context): String {
        return when (healthInt) {
            BatteryManager.BATTERY_HEALTH_COLD -> context.getString(R.string.cold)
            BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.good)
            BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.dead)
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.overheat)
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> context.getString(R.string.overvoltage)
            BatteryManager.BATTERY_HEALTH_UNKNOWN -> context.getString(R.string.unknown)
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> context.getString(R.string.unspecified_failure)
            else -> context.getString(R.string.not_available)
        }
    }

    fun getBatteryLevel(context: Context): Float {
        kotlin.runCatching {
            val batteryStatus = getBatteryStatusIntent(context)!!
            val level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1)

            if (level != -1 && scale != -1) {
                val batteryPct = level / scale.toFloat() * 100.0F
                return batteryPct.round2()
            }
        }.onFailure {
            return -1.0F
        }

        return -1.0F
    }

    fun getBatteryDrawable(context: Context): Int {
        return when (getBatteryLevel(context)) {
            in 0F..10F -> R.drawable.ic_battery_alert
            in 11F..20F -> R.drawable.ic_battery_1
            in 21F..32F -> R.drawable.ic_battery_2
            in 33F..44F -> R.drawable.ic_battery_3
            in 45F..58F -> R.drawable.ic_battery_4
            in 59F..80F -> R.drawable.ic_battery_5
            in 81F..94F -> R.drawable.ic_battery_6
            in 95F..100F -> R.drawable.ic_battery_full
            else -> R.drawable.ic_battery_unknown
        }
    }
}