package app.simple.inure.factories.panels

import android.content.pm.PackageInfo
import androidx.annotation.Nullable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.viewers.XMLViewerViewModel

class XMLViewerViewModelFactory(val packageInfo: PackageInfo, private val isManifest: Boolean, @Nullable private val pathToXml: String, private val accentColor: Int)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        @Suppress("UNCHECKED_CAST") // Cast is checked
        when {
            modelClass.isAssignableFrom(XMLViewerViewModel::class.java) -> {
                return XMLViewerViewModel(packageInfo, isManifest, pathToXml, application, accentColor) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}