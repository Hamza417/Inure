package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.SensorsPreferences
import app.simple.inure.preferences.SharedPreferences.getSharedPreferences
import app.simple.inure.util.SortSensors.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SensorsViewModel(application: Application) : WrappedViewModel(application), SharedPreferences.OnSharedPreferenceChangeListener {

    init {
        getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
    }

    private val sensors: MutableLiveData<MutableList<Sensor>> by lazy {
        MutableLiveData<MutableList<Sensor>>().also {
            loadSensorData()
        }
    }

    fun getSensorsData(): LiveData<MutableList<Sensor>> = sensors

    private fun loadSensorData() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                with(getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager) {
                    val list: MutableList<Sensor> = getSensorList(Sensor.TYPE_ALL).toMutableList()

                    list.getSortedList(SensorsPreferences.getSortStyle())

                    this@SensorsViewModel.sensors.postValue(list)
                }
            }.onFailure {
                postError(it)
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            SensorsPreferences.isSortingReversed,
            SensorsPreferences.sortStyle -> {
                loadSensorData()
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}