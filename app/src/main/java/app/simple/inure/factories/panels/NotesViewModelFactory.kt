package app.simple.inure.factories.panels

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.panels.NotesEditorViewModel

class NotesViewModelFactory(private val packageInfo: PackageInfo) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

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