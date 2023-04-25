package app.simple.inure.factories.subpanels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.subviewers.AnalyticsSDKViewModel
import com.github.mikephil.charting.data.Entry

class AnalyticsSDKViewModelFactory(private val entry: Entry) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        when {
            modelClass.isAssignableFrom(AnalyticsSDKViewModel::class.java) -> {
                return AnalyticsSDKViewModel(application, entry) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}