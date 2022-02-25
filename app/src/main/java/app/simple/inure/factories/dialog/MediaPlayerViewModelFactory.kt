package app.simple.inure.factories.dialog

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * This class is deprecated in favour of using foreground service.
 * Refer to the [app.simple.inure.services.AudioService] class to read
 * the updated implementation.
 */
@Deprecated("Use ", replaceWith = ReplaceWith("AudioService"))
class MediaPlayerViewModelFactory(private val application: Application, private val uri: Uri) :
        ViewModelProvider.AndroidViewModelFactory(application) {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        when {
            modelClass.isAssignableFrom(AudioService::class.java) -> {
                return AudioService(application, uri) as T
            }
            else -> {
                throw IllegalArgumentException("Nope!!, Wrong Viewmodel")
            }
        }
    }
}