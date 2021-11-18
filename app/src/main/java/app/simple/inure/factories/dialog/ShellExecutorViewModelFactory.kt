package app.simple.inure.factories.dialog

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.dialogs.ShellExecutorViewModel

class ShellExecutorViewModelFactory(private val command: String, private val application: Application) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShellExecutorViewModel::class.java)) {
            return ShellExecutorViewModel(application, command) as T
        } else {
            throw IllegalArgumentException("Nope!!, Wrong Viewmodel!!")
        }
    }
}