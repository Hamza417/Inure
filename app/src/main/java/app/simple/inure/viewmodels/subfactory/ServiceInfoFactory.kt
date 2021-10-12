package app.simple.inure.viewmodels.subfactory

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.model.ServiceInfoModel
import app.simple.inure.viewmodels.subviewers.ServiceInfoViewModel

class ServiceInfoFactory(private val application: Application, private val serviceInfoModel: ServiceInfoModel, val packageInfo: PackageInfo) : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
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