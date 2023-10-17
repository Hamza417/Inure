package app.simple.inure.factories.batch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.batch.BatchTrackersViewModel

class BatchTrackersFactory(private val packages: ArrayList<String>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        @Suppress("UNCHECKED_CAST")
        return BatchTrackersViewModel(application, packages) as T
    }
}