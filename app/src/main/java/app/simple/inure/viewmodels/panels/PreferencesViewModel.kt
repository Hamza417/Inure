package app.simple.inure.viewmodels.panels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extension.viewmodels.WrappedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferencesViewModel(application: Application) : WrappedViewModel(application) {

    private val preferences: MutableLiveData<ArrayList<Pair<Int, Int>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Int>>>().also {
            loadPreferencesData()
        }
    }

    fun getPreferences(): LiveData<ArrayList<Pair<Int, Int>>> {
        return preferences
    }

    private fun loadPreferencesData() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = context

            val list = arrayListOf(
                    Pair(R.drawable.ic_appearance, R.string.appearance),
                    Pair(R.drawable.ic_behaviour, R.string.behaviour),
                    Pair(R.drawable.ic_app_settings, R.string.configuration),
                    Pair(R.drawable.ic_formatting, R.string.formatting),
                    Pair(R.drawable.ic_accessibility, R.string.accessibility),
                    Pair(R.drawable.ic_terminal_black, R.string.terminal),
                    Pair(R.drawable.ic_shell, R.string.shell_preferences),
                    Pair(R.drawable.ic_adb, R.string.development),
                    Pair(R.drawable.ic_audio_placeholder, R.string.about)
            )

            preferences.postValue(list)
        }
    }
}