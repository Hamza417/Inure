package app.simple.inure.factories.actions

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.models.PermissionInfo
import app.simple.inure.viewmodels.dialogs.PermissionStatusViewModel

class PermissionStatusFactory(private val packageInfo: PackageInfo, private val permissionInfo: PermissionInfo, val mode: String?) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

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