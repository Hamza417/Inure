package app.simple.inure.viewmodels.factory

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.dialogs.FilePreparingViewModel
import app.simple.inure.viewmodels.panels.ApkDataViewModel
import java.lang.IllegalArgumentException

class ApplicationInfoFactory(private val application: Application, private val param: ApplicationInfo)
    : ViewModelProvider.AndroidViewModelFactory(application) {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST") // Cast is checked
        when {
            modelClass.isAssignableFrom(FilePreparingViewModel::class.java) -> {
                return FilePreparingViewModel(application, param) as T
            }
            modelClass.isAssignableFrom(ApkDataViewModel::class.java) -> {
                return ApkDataViewModel(application, param) as T
            }
            else -> {
                /**
                 * This viewmodel factory is specific to
                 * [FilePreparingViewModel] and assigning it properly
                 * won't throw this exception
                 */
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}
