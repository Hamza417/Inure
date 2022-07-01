package app.simple.inure.factories.subpanels

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.viewmodels.subviewers.ActivityInfoViewModel

class ActivityInfoFactory(private val activityInfoModel: ActivityInfoModel, val packageInfo: PackageInfo)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

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