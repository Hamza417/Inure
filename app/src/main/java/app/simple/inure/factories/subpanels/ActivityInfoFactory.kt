package app.simple.inure.factories.subpanels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.viewmodels.subviewers.ActivityInfoViewModel

class ActivityInfoFactory(private val application: Application, private val activityInfoModel: ActivityInfoModel, val packageInfo: PackageInfo)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(ActivityInfoViewModel::class.java) -> {
                return ActivityInfoViewModel(application, activityInfoModel, packageInfo) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}