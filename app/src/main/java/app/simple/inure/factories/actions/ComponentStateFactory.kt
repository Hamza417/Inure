package app.simple.inure.factories.actions

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.dialogs.ComponentStateViewModel

class ComponentStateFactory(private val application: Application, private val packageInfo: PackageInfo, private val packageId: String, val mode: Boolean) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(ComponentStateViewModel::class.java) -> {
                return ComponentStateViewModel(application, packageInfo, packageId, mode) as T
            }
            else -> {
                throw IllegalArgumentException("Wrong viewmodel.")
            }
        }
    }
}