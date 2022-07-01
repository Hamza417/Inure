package app.simple.inure.factories.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.dialogs.ShellExecutorViewModel

class ShellExecutorViewModelFactory(private val command: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        if (modelClass.isAssignableFrom(ShellExecutorViewModel::class.java)) {
            return ShellExecutorViewModel(application, command) as T
        } else {
            throw IllegalArgumentException("Nope!!, Wrong Viewmodel!!")
        }
    }
}