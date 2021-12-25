package app.simple.inure.factories.subpanels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.models.ProviderInfoModel
import app.simple.inure.viewmodels.subviewers.ProviderInfoViewModel

class ProviderInfoFactory(private val application: Application, private val providerInfoModel: ProviderInfoModel, val packageInfo: PackageInfo)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(ProviderInfoViewModel::class.java) -> {
                return ProviderInfoViewModel(application, providerInfoModel) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}