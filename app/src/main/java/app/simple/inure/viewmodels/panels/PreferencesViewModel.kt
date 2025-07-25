package app.simple.inure.viewmodels.panels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.constants.PreferencesSearchConstants
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.PreferenceModel
import app.simple.inure.preferences.TrialPreferences
import app.simple.inure.util.ConditionUtils.invert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PreferencesViewModel(application: Application) : WrappedViewModel(application) {

    var keyword: String? = null
        set(value) {
            field = value
            loadPreferencesSearchData()
        }

    private val preferences: MutableLiveData<ArrayList<Pair<Int, Int>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Int>>>().also {
            loadPreferencesData()
        }
    }

    private val preferencesSearchData: MutableLiveData<ArrayList<PreferenceModel>> by lazy {
        MutableLiveData<ArrayList<PreferenceModel>>()
    }

    fun getPreferences(): LiveData<ArrayList<Pair<Int, Int>>> {
        return preferences
    }

    fun getPreferencesSearchData(): LiveData<ArrayList<PreferenceModel>> {
        return preferencesSearchData
    }

    private fun loadPreferencesData() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<Pair<Int, Int>>()

            if (TrialPreferences.isFullVersion().invert()) {
                list.add(Pair(R.drawable.ic_sell, R.string.purchase))
            }

            list.add(Pair(0, 0)) // Divider

            list.add(Pair(R.drawable.ic_appearance, R.string.appearance))
            list.add(Pair(R.drawable.ic_behaviour, R.string.behavior))
            list.add(Pair(R.drawable.ic_app_settings, R.string.configuration))
            list.add(Pair(R.drawable.ic_formatting, R.string.formatting))
            list.add(Pair(R.drawable.ic_accessibility, R.string.accessibility))
            list.add(Pair(0, 0)) // Divider
            list.add(Pair(R.drawable.ic_terminal_black, R.string.terminal))
            list.add(Pair(R.drawable.ic_shell, R.string.shell))
            list.add(Pair(0, 0)) // Divider
            list.add(Pair(R.drawable.ic_sd_storage, R.string.manage_space))
            list.add(Pair(0, 0))
            list.add(Pair(R.drawable.ic_data_object, R.string.development))
            list.add(Pair(R.drawable.ic_info, R.string.about))

            preferences.postValue(list)
        }
    }

    private fun loadPreferencesSearchData() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<PreferenceModel>()

            if (keyword.isNullOrEmpty()) {
                loadPreferencesData()
            } else {
                val context = context

                for (prefs in PreferencesSearchConstants.preferencesStructureData) {
                    if (context.getString(prefs.title).lowercase().contains(keyword!!.lowercase()) ||
                            context.getString(prefs.description).lowercase().contains(keyword!!.lowercase()) ||
                            context.getString(prefs.category).lowercase().contains(keyword!!.lowercase()) ||
                            context.getString(prefs.type).lowercase().contains(keyword!!.lowercase()) ||
                            context.getString(prefs.panel).lowercase().contains(keyword!!.lowercase())) {

                        list.add(prefs)
                    }
                }
            }

            preferencesSearchData.postValue(list)
        }
    }
}
