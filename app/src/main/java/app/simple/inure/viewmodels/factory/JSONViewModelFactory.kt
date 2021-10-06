package app.simple.inure.viewmodels.factory

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.viewers.JSONViewerViewModel

class JSONViewModelFactory(private val application: Application, private val packageInfo: PackageInfo, private val accentColor: Int, private val path: String)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(JSONViewerViewModel::class.java) -> {
                return JSONViewerViewModel(application, accentColor, packageInfo, path) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}