package app.simple.inure.factories.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.batch.BatchInstallerViewModel

/**
 * [ViewModelProvider.Factory] for [BatchInstallerViewModel].
 *
 * @param paths List of absolute APK file paths to install in batch.
 *
 * @author Hamza417
 */
class BatchInstallerFactory(private val paths: ArrayList<String>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        @Suppress("UNCHECKED_CAST")
        return BatchInstallerViewModel(application, paths) as T
    }
}

