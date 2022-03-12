package app.simple.inure.factories.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.viewers.AppStatisticsViewModel

class AppStatisticsViewModelFactory(private val application: Application, private val packageInfo: PackageInfo) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(AppStatisticsViewModel::class.java) -> {
                return AppStatisticsViewModel(application, packageInfo) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong ViewModel!!")
            }
        }
    }
}