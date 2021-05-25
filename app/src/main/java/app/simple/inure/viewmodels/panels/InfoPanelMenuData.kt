package app.simple.inure.viewmodels.panels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.preferences.ConfigurationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoPanelMenuData(application: Application) : AndroidViewModel(application) {
    private val menuItems: MutableLiveData<List<Pair<Int, String>>> by lazy {
        MutableLiveData<List<Pair<Int, String>>>().also {
            loadItems()
        }
    }

    private val menuOptions: MutableLiveData<List<Pair<Int, String>>> by lazy {
        MutableLiveData<List<Pair<Int, String>>>().also {
            loadOptions()
        }
    }

    fun getMenuItems(): LiveData<List<Pair<Int, String>>> {
        return menuItems
    }

    fun getMenuOptions(): LiveData<List<Pair<Int, String>>> {
        return menuOptions
    }

    private fun loadOptions() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>().applicationContext

            val list = if(ConfigurationPreferences.isUsingRoot()) {
                listOf(
                    Pair(R.drawable.ic_launch, context.getString(R.string.launch)),
                    Pair(R.drawable.ic_send, context.getString(R.string.send)),
                    Pair(R.drawable.ic_delete, context.getString(R.string.uninstall)),
                    Pair(R.drawable.ic_delete_sweep, context.getString(R.string.clear_data)),
                    Pair(R.drawable.ic_broom, context.getString(R.string.clear_cache))
                )
            } else {
                listOf(
                    Pair(R.drawable.ic_launch, context.getString(R.string.launch)),
                    Pair(R.drawable.ic_send, context.getString(R.string.send)),
                    Pair(R.drawable.ic_delete, context.getString(R.string.uninstall)),
                )
            }

            menuOptions.postValue(list)
        }
    }

    private fun loadItems() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = getApplication<Application>().applicationContext

            val list = listOf(
                Pair(R.drawable.ic_permission, context.getString(R.string.permissions)),
                Pair(R.drawable.ic_activities, context.getString(R.string.activities)),
                Pair(R.drawable.ic_services, context.getString(R.string.services)),
                Pair(R.drawable.ic_certificate, context.getString(R.string.certificate)),
                Pair(R.drawable.ic_resources, context.getString(R.string.resources)),
                Pair(R.drawable.ic_broadcast, context.getString(R.string.broadcasts)),
                Pair(R.drawable.ic_provider, context.getString(R.string.providers)),
                Pair(R.drawable.ic_file_xml, context.getString(R.string.manifest)),
                Pair(R.drawable.ic_anchor, context.getString(R.string.uses_feature)),
                Pair(R.drawable.ic_graphics, context.getString(R.string.graphics)),
                Pair(R.drawable.ic_extras, context.getString(R.string.extras))
            )

            menuItems.postValue(list)
        }
    }
}