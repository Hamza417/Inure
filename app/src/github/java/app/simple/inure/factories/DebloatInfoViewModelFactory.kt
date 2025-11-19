package app.simple.inure.factories

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.dialogs.DebloatInfoViewModel

class DebloatInfoViewModelFactory(private val packageInfo: PackageInfo) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(DebloatInfoViewModel::class.java) -> {
                return DebloatInfoViewModel(application, packageInfo) as T
            }
            else -> {
                throw IllegalArgumentException("Wrong viewmodel")
            }
        }
    }
}