package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SensorsViewModel(application: Application) : AndroidViewModel(application) {

    private val sensors: MutableLiveData<MutableList<Sensor>> by lazy {
        MutableLiveData<MutableList<Sensor>>().also {
            loadSensorData()
        }
    }

    fun getSensorsData(): LiveData<MutableList<Sensor>> = sensors

    private fun loadSensorData() {
        viewModelScope.launch(Dispatchers.IO) {
            with(getApplication<Application>().getSystemService(Context.SENSOR_SERVICE) as SensorManager) {
                val list: MutableList<Sensor> = getSensorList(Sensor.TYPE_ALL).toMutableList()

                list.sortBy {
                    it.name.lowercase()
                }

                this@SensorsViewModel.sensors.postValue(list)
            }
        }
    }
}