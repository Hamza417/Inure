package app.simple.inure.factories.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.viewmodels.panels.NotesEditorViewModel

class NotesViewModelFactory(private val application: Application, private val packageInfo: PackageInfo) : ViewModelProvider.AndroidViewModelFactory(application) {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(NotesEditorViewModel::class.java) -> {
                return NotesEditorViewModel(application, packageInfo) as T
            }
            else -> {
                throw IllegalArgumentException("Wrong Viewmodel!!")
            }
        }
    }
}