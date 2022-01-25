package app.simple.inure.factories.deviceinfo

import android.app.Application
import android.view.Window
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.deviceinfo.DeviceInfoViewModel

class DeviceInfoFactory(private val application: Application, private val window: Window)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(DeviceInfoViewModel::class.java) -> {
                return DeviceInfoViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}