package app.simple.inure.factories.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.viewers.XMLViewerData

class XmlDataFactory(val packageInfo: PackageInfo, private val isManifest: Boolean, @Nullable private val pathToXml: String, val application: Application, private val accentColor: Int)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") // Cast is checked
        when {
            modelClass.isAssignableFrom(XMLViewerData::class.java) -> {
                return XMLViewerData(packageInfo, isManifest, pathToXml, application, accentColor) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}