package app.simple.inure.viewmodels.deviceinfo

import android.app.Application
import android.os.BatteryManager
import android.text.Spannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.util.BatteryUtils
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BatteryViewModel(application: Application) : WrappedViewModel(application) {

    private val basics: MutableLiveData<ArrayList<Pair<String, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, Spannable>>>().also {
            loadBasics()
        }
    }

    fun getBasics(): LiveData<ArrayList<Pair<String, Spannable>>> {
        return basics
    }

    private fun loadBasics() {
        viewModelScope.launch(Dispatchers.Default) {
            basics.postValue(arrayListOf(
                    getLevel(),
                    getCapacity(),
                    getVoltage(),
                    getHealth(),
                    getTemperature(),
                    getCharging(),
                    getChargingType(),
                    getTechnology()
            ))
        }
    }

    private fun getLevel(): Pair<String, Spannable> {
        kotlin.runCatching {
            return Pair(getString(R.string.level),
                        "${BatteryUtils.getBatteryLevel(applicationContext())}%".applySecondaryTextColor())
        }

        return Pair(getString(R.string.level),
                    getString(R.string.unknown).applySecondaryTextColor())
    }

    private fun getCapacity(): Pair<String, Spannable> {
        return Pair(getString(R.string.capacity),
                    "${BatteryUtils.getBatteryCapacity(applicationContext())} mAh".applySecondaryTextColor())
    }

    private fun getHealth(): Pair<String, Spannable> {
        val health = BatteryUtils.getBatteryStatusIntent(applicationContext())?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
        return Pair(getString(R.string.health),
                    BatteryUtils.getBatteryHealthStatus(health ?: -1, context).applySecondaryTextColor())
    }

    private fun getVoltage(): Pair<String, Spannable> {
        val voltage = BatteryUtils.getBatteryStatusIntent(applicationContext())?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1)
        return Pair(getString(R.string.voltage),
                    "${voltage ?: -1 / 1000.0}V".applySecondaryTextColor())
    }

    private fun getTemperature(): Pair<String, Spannable> {
        val batteryStatus = BatteryUtils.getBatteryStatusIntent(applicationContext())
        val temp = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10
        return Pair(getString(R.string.temperature),
                    "$temp°C".applySecondaryTextColor())
    }

    private fun getTechnology(): Pair<String, Spannable> {
        val technology = BatteryUtils.getBatteryStatusIntent(applicationContext())?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)
        return Pair(getString(R.string.technology),
                    technology!!.applySecondaryTextColor())
    }

    private fun getCharging(): Pair<String, Spannable> {
        val status = BatteryUtils.getBatteryStatusIntent(applicationContext())?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
        val charging =
            if (isCharging) getString(R.string.yes)
            else getString(R.string.no)

        return Pair(getString(R.string.charging),
                    charging!!.applySecondaryTextColor())
    }

    private fun getChargingType(): Pair<String, Spannable> {
        val chargePlug = BatteryUtils.getBatteryStatusIntent(applicationContext())?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
        val usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB
        val acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC

        val chargingType: String = when {
            usbCharge -> "USB"
            acCharge -> "AC"
            else -> getString(R.string.unknown)
        }

        return Pair(getString(R.string.charging_type),
                    chargingType.applySecondaryTextColor())
    }
}