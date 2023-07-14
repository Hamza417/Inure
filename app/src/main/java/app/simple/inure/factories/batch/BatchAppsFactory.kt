package app.simple.inure.factories.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.viewmodels.dialogs.BatchBatteryOptimizationViewModel
import app.simple.inure.viewmodels.dialogs.BatchForceStopViewModel

class BatchAppsFactory(private val apps: ArrayList<BatchPackageInfo>) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
        when {
            modelClass.isAssignableFrom(BatchBatteryOptimizationViewModel::class.java) -> {
                return BatchBatteryOptimizationViewModel(application, apps) as T
            }

            modelClass.isAssignableFrom(BatchForceStopViewModel::class.java) -> {
                return BatchForceStopViewModel(application, apps) as T
            }

            else -> {
                throw IllegalArgumentException("Wrong viewmodel")
            }
        }
    }
}