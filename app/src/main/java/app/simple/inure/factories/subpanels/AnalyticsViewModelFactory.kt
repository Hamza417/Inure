package app.simple.inure.factories.subpanels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import app.simple.inure.viewmodels.subviewers.AnalyticsDataViewModel
import app.simple.inure.viewmodels.subviewers.AnalyticsInstallerViewModel
import com.github.mikephil.charting.data.Entry

class AnalyticsViewModelFactory(private val entry: Entry) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        val application = extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]!!

        @Suppress("LiftReturnOrAssignment")
        when {
            modelClass.isAssignableFrom(AnalyticsDataViewModel::class.java) -> {
                return AnalyticsDataViewModel(application, entry) as T
            }
            modelClass.isAssignableFrom(AnalyticsInstallerViewModel::class.java) -> {
                return AnalyticsInstallerViewModel(application, entry) as T
            }
            else -> {
                throw IllegalArgumentException("Nope, Wrong Viewmodel!!")
            }
        }
    }
}