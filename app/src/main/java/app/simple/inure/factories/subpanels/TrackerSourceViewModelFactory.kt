package app.simple.inure.factories.subpanels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.subviewers.TrackerSourceViewModel

class TrackerSourceViewModelFactory(private val application: Application, private val className: String, val packageInfo: PackageInfo, val accentColor: Int)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(TrackerSourceViewModel::class.java) -> {
                return TrackerSourceViewModel(application, className, packageInfo, accentColor) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}