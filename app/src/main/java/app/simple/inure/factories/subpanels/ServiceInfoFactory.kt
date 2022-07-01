package app.simple.inure.factories.subpanels

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.models.ServiceInfoModel
import app.simple.inure.viewmodels.subviewers.ServiceInfoViewModel

class ServiceInfoFactory(private val serviceInfoModel: ServiceInfoModel, val packageInfo: PackageInfo)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(ServiceInfoViewModel::class.java) -> {
                return ServiceInfoViewModel(application, serviceInfoModel, packageInfo) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}