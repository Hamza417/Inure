package app.simple.inure.factories.actions

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.dialogs.ForceCloseViewModel

class ForceCloseViewModelFactory(private val application: Application, private val packageInfo: PackageInfo) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ForceCloseViewModel(application, packageInfo) as T
    }
}