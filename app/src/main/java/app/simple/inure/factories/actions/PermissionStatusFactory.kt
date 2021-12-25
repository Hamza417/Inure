package app.simple.inure.factories.actions

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.models.PermissionInfo
import app.simple.inure.viewmodels.dialogs.PermissionStatusViewModel

class PermissionStatusFactory internal constructor(private val application: Application, private val packageInfo: PackageInfo, private val permissionInfo: PermissionInfo, val mode: String?) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(PermissionStatusViewModel::class.java) -> {
                return PermissionStatusViewModel(application, packageInfo, permissionInfo, mode) as T
            }
            else -> {
                throw IllegalArgumentException("Wrong View Model")
            }
        }
    }
}