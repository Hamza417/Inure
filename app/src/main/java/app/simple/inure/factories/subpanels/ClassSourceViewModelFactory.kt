package app.simple.inure.factories.subpanels

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.subviewers.ClassSourceViewModel

class ClassSourceViewModelFactory(private val className: String, val packageInfo: PackageInfo)
    : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(ClassSourceViewModel::class.java) -> {
                return ClassSourceViewModel(application, className, packageInfo) as T
            }

            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}