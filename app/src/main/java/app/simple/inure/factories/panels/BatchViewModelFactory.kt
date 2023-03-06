package app.simple.inure.factories.panels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.viewmodels.panels.BatchUninstallerShizukuViewModel

class BatchViewModelFactory(private val list: ArrayList<BatchPackageInfo>) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(BatchUninstallerShizukuViewModel::class.java) -> {
                return BatchUninstallerShizukuViewModel(application, list) as T
            }
            else -> {
                throw java.lang.IllegalArgumentException("Nope, Wrong ViewModel!!")
            }
        }
    }
}