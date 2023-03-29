package app.simple.inure.factories.actions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.viewmodels.dialogs.BatchBatteryOptimizationViewModel

class BatchBatteryOptimizationFactory(private val apps: ArrayList<BatchPackageInfo>) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!
        return BatchBatteryOptimizationViewModel(application, apps) as T
    }
}