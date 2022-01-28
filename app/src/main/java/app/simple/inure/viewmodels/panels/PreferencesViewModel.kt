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

    private val preferences: MutableLiveData<ArrayList<Pair<Int, String>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, String>>>().also {
            loadPreferencesData()
        }
    }

    fun getPreferences(): LiveData<ArrayList<Pair<Int, String>>> {
        return preferences
    }

    private fun loadPreferencesData() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = context

            val list = arrayListOf(
                    Pair(R.drawable.ic_appearance, context.getString(R.string.appearance)),
                    Pair(R.drawable.ic_behaviour, context.getString(R.string.behaviour)),
                    Pair(R.drawable.ic_app_settings, context.getString(R.string.configuration)),
                    Pair(R.drawable.ic_accessibility, context.getString(R.string.accessibility)),
                    Pair(R.drawable.ic_terminal_black, context.getString(R.string.terminal)),
                    Pair(R.drawable.ic_shell, context.getString(R.string.shell_preferences)),
                    Pair(R.drawable.ic_about, context.getString(R.string.about))
            )

            preferences.postValue(list)
        }
    }
}