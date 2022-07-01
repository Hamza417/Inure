package app.simple.inure.factories.actions

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.dialogs.ActivityLauncherViewModel

class ActivityLaunchFactory(private val packageInfo: PackageInfo, private val packageId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(ActivityLauncherViewModel::class.java) -> {
                return ActivityLauncherViewModel(application, packageInfo, packageId) as T
            }
            else -> {
                throw IllegalArgumentException("Wrong viewmodel")
            }
        }
    }
}