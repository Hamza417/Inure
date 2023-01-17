package app.simple.inure.factories.viewers

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.viewers.AudioPlayerViewModel

class AudioPlayerViewModelFactory(private val uri: Uri) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(AudioPlayerViewModel::class.java) -> {
                @Suppress("UNCHECKED_CAST")
                return AudioPlayerViewModel(application, uri) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}