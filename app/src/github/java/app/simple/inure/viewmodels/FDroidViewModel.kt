package app.simple.inure.viewmodels

import android.app.Application
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FDroidViewModel(application: Application) : WrappedViewModel(application) {

    private fun loadFDroidRepositories() {
        viewModelScope.launch(Dispatchers.IO) {

        }
    }
}