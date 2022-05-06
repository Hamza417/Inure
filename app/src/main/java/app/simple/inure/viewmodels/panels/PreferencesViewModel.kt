package app.simple.inure.viewmodels.panels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.models.PreferenceSearchModel
import app.simple.inure.preferences.PreferencesSearchData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferencesViewModel(application: Application) : WrappedViewModel(application) {

    var keyword: String? = null
        set(value) {
            field = value
            loadPreferencesSearchData()
        }

    private val preferencesStructureData =
        PreferencesSearchData.appearanceData +
                PreferencesSearchData.behaviourData +
                PreferencesSearchData.configurationData +
                PreferencesSearchData.formattingData +
                PreferencesSearchData.accessibilityData +
                PreferencesSearchData.terminalData +
                PreferencesSearchData.shellData +
                PreferencesSearchData.developmentData +
                PreferencesSearchData.aboutData

    private val preferences: MutableLiveData<ArrayList<Pair<Int, Int>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Int>>>().also {
            loadPreferencesData()
        }
    }

    private val preferencesSearchData: MutableLiveData<ArrayList<PreferenceSearchModel>> by lazy {
        MutableLiveData<ArrayList<PreferenceSearchModel>>()
    }

    fun getPreferences(): LiveData<ArrayList<Pair<Int, Int>>> {
        return preferences
    }

    fun getPreferencesSearchData(): LiveData<ArrayList<PreferenceSearchModel>> {
        return preferencesSearchData
    }

    private fun loadPreferencesData() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf(
                    Pair(R.drawable.ic_appearance, R.string.appearance),
                    Pair(R.drawable.ic_behaviour, R.string.behaviour),
                    Pair(R.drawable.ic_app_settings, R.string.configuration),
                    Pair(R.drawable.ic_formatting, R.string.formatting),
                    Pair(R.drawable.ic_accessibility, R.string.accessibility),
                    Pair(0, 0), // Divider
                    Pair(R.drawable.ic_terminal_black, R.string.terminal),
                    Pair(R.drawable.ic_shell, R.string.shell_preferences),
                    Pair(R.drawable.ic_layers, R.string.batch),
                    Pair(0, 0), // Divider
                    Pair(R.drawable.ic_adb, R.string.development),
                    Pair(R.drawable.ic_audio_placeholder, R.string.about)
            )

            preferences.postValue(list)
        }
    }

    private fun loadPreferencesSearchData() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<PreferenceSearchModel>()

            if (keyword.isNullOrEmpty()) {
                loadPreferencesData()
            } else {
                val context = context

                for (prefs in preferencesStructureData) {
                    if (context.getString(prefs.title).lowercase().contains(keyword!!) ||
                        context.getString(prefs.description).lowercase().contains(keyword!!) ||
                        context.getString(prefs.category).lowercase().contains(keyword!!) ||
                        context.getString(prefs.type).lowercase().contains(keyword!!) ||
                        context.getString(prefs.panel).lowercase().contains(keyword!!)) {

                        list.add(prefs)
                    }
                }
            }

            preferencesSearchData.postValue(list)
        }
    }
}