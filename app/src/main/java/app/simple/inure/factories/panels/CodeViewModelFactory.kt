package app.simple.inure.factories.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.viewers.JSONViewerViewModel
import app.simple.inure.viewmodels.viewers.JavaViewModel

class CodeViewModelFactory(private val application: Application, private val packageInfo: PackageInfo, private val accentColor: Int, private val path: String)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(JSONViewerViewModel::class.java) -> {
                return JSONViewerViewModel(application, accentColor, packageInfo, path) as T
            }
            modelClass.isAssignableFrom(JavaViewModel::class.java) -> {
                return JavaViewModel(application, accentColor, packageInfo, path) as T
            }
            else -> {
                throw IllegalArgumentException("Wrong Viewmodel!!")
            }
        }
    }
}