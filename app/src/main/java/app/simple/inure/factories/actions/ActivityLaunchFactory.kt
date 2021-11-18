package app.simple.inure.factories.actions

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.dialogs.ActivityLauncherViewModel

class ActivityLaunchFactory(private val application: Application, private val packageInfo: PackageInfo, private val packageId: String) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
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