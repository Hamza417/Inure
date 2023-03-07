package app.simple.inure.factories.actions

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.dialogs.ComponentStateViewModel

class ComponentStateFactory(private val packageInfo: PackageInfo, private val packageId: String, val mode: Boolean) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

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