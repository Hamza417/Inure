package app.simple.inure.viewmodels.factory

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.panels.FontData

class FontViewModelFactory(val application: Application, val path: String, val applicationInfo: ApplicationInfo, val color: Int)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") // Cast is checked
        when {
            modelClass.isAssignableFrom(FontData::class.java) -> {
                return FontData(application, path, applicationInfo, color) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}