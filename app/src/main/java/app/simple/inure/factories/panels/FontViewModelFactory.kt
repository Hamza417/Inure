package app.simple.inure.factories.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.viewers.FontViewModel

class FontViewModelFactory(val application: Application, val path: String, val packageInfo: PackageInfo, val color: Int)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") // Cast is checked
        when {
            modelClass.isAssignableFrom(FontViewModel::class.java) -> {
                return FontViewModel(application, path, packageInfo, color) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}