package app.simple.inure.factories.panels

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.panels.AppInfoViewModel
import app.simple.inure.viewmodels.viewers.ActivitiesViewModel
import app.simple.inure.viewmodels.viewers.ApkDataViewModel
import app.simple.inure.viewmodels.viewers.AppInformationViewModel
import app.simple.inure.viewmodels.viewers.BootManagerViewModel
import app.simple.inure.viewmodels.viewers.BootViewModel
import app.simple.inure.viewmodels.viewers.CertificatesViewModel
import app.simple.inure.viewmodels.viewers.DexDataViewModel
import app.simple.inure.viewmodels.viewers.ExtrasViewModel
import app.simple.inure.viewmodels.viewers.GraphicsViewModel
import app.simple.inure.viewmodels.viewers.IFWViewerViewModel
import app.simple.inure.viewmodels.viewers.OperationsViewModel
import app.simple.inure.viewmodels.viewers.PermissionsViewModel
import app.simple.inure.viewmodels.viewers.ProvidersViewModel
import app.simple.inure.viewmodels.viewers.ReceiversViewModel
import app.simple.inure.viewmodels.viewers.ServicesViewModel
import app.simple.inure.viewmodels.viewers.SharedLibrariesViewModel
import app.simple.inure.viewmodels.viewers.SharedPreferencesViewModel
import app.simple.inure.viewmodels.viewers.TrackersViewModel

class PackageInfoFactory(private val packageInfo: PackageInfo) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        @Suppress("UNCHECKED_CAST") // Cast is checked
        when {
            modelClass.isAssignableFrom(ApkDataViewModel::class.java) -> {
                return ApkDataViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(AppInfoViewModel::class.java) -> {
                return AppInfoViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(AppInformationViewModel::class.java) -> {
                return AppInformationViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(CertificatesViewModel::class.java) -> {
                return CertificatesViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(DexDataViewModel::class.java) -> {
                return DexDataViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(ActivitiesViewModel::class.java) -> {
                return ActivitiesViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(ExtrasViewModel::class.java) -> {
                return ExtrasViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(GraphicsViewModel::class.java) -> {
                return GraphicsViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(PermissionsViewModel::class.java) -> {
                return PermissionsViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(ServicesViewModel::class.java) -> {
                return ServicesViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(ProvidersViewModel::class.java) -> {
                return ProvidersViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(ReceiversViewModel::class.java) -> {
                return ReceiversViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(SharedLibrariesViewModel::class.java) -> {
                return SharedLibrariesViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(TrackersViewModel::class.java) -> {
                return TrackersViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(OperationsViewModel::class.java) -> {
                return OperationsViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(SharedPreferencesViewModel::class.java) -> {
                return SharedPreferencesViewModel(packageInfo, application) as T
            }
            modelClass.isAssignableFrom(BootManagerViewModel::class.java) -> {
                return BootManagerViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(IFWViewerViewModel::class.java) -> {
                return IFWViewerViewModel(application, packageInfo) as T
            }
            modelClass.isAssignableFrom(BootViewModel::class.java) -> {
                return BootViewModel(application, packageInfo) as T
            }
            else -> {
                throw IllegalArgumentException("Nope!!, Wrong Viewmodel!!")
            }
        }
    }
}
