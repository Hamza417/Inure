package app.simple.inure.factories.panels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.viewmodels.dialogs.BatchStateViewModel
import app.simple.inure.viewmodels.dialogs.BatchUninstallerViewModel

class BatchViewModelFactory(private val list: ArrayList<BatchPackageInfo>, val state: Boolean) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        return when {
            modelClass.isAssignableFrom(BatchUninstallerViewModel::class.java) -> {
                BatchUninstallerViewModel(application, list) as T
            }
            modelClass.isAssignableFrom(BatchStateViewModel::class.java) -> {
                BatchStateViewModel(application, list, state) as T
            }
            else -> {
                throw java.lang.IllegalArgumentException("Nope, Wrong ViewModel!!")
            }
        }
    }
}