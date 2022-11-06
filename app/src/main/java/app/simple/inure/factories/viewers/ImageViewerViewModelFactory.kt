package app.simple.inure.factories.viewers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.viewers.ImageViewerViewModel

class ImageViewerViewModelFactory(val pathToImage: String, val pathToApk: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(ImageViewerViewModel::class.java) -> {
                return ImageViewerViewModel(application, pathToImage, pathToApk) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}