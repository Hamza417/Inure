package app.simple.inure.viewmodels.factory

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.viewers.XMLViewerData

class XmlDataFactory(
        val applicationInfo: ApplicationInfo,
        private val isManifest: Boolean,
        @Nullable private val pathToXml: String,
        val application: Application,
        private val accentColor: Int,
) : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") // Cast is checked
        when {
            modelClass.isAssignableFrom(XMLViewerData::class.java) -> {
                return XMLViewerData(applicationInfo, isManifest, pathToXml, application, accentColor) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}