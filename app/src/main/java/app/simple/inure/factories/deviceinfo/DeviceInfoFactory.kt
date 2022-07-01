package app.simple.inure.factories.deviceinfo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.deviceinfo.DeviceInfoViewModel

class DeviceInfoFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(DeviceInfoViewModel::class.java) -> {
                return DeviceInfoViewModel(application) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel")
        }
    }
}