package app.simple.inure.factories.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.viewers.JSONViewerViewModel
import app.simple.inure.viewmodels.viewers.JavaViewModel

class CodeViewModelFactory(private val application: Application, private val packageInfo: PackageInfo, private val accentColor: Int, private val path: String)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        return when {
            modelClass.isAssignableFrom(JSONViewerViewModel::class.java) -> {
                JSONViewerViewModel(application, accentColor, packageInfo, path) as T
            }
            modelClass.isAssignableFrom(JavaViewModel::class.java) -> {
                JavaViewModel(application, accentColor, packageInfo, path) as T
            }
            else -> {
                throw IllegalArgumentException("Wrong Viewmodel!!")
            }
        }
    }
}