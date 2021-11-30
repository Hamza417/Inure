package app.simple.inure.factories.dialog

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.dialogs.ErrorViewModel

class ErrorViewModelFactory(private val application: Application, private val error: String, val accentColor: Int) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ErrorViewModel(application, error, accentColor) as T
    }
}