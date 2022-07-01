package app.simple.inure.factories.subpanels

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.models.ProviderInfoModel
import app.simple.inure.viewmodels.subviewers.ProviderInfoViewModel

class ProviderInfoFactory(private val providerInfoModel: ProviderInfoModel, val packageInfo: PackageInfo)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

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