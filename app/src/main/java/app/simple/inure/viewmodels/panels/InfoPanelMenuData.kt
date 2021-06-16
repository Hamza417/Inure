package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.preferences.ConfigurationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class InfoPanelMenuData(application: Application, val applicationInfo: ApplicationInfo) : AndroidViewModel(application) {
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

    fun loadOptions() {
        viewModelScope.launch(Dispatchers.Default) {

            val context = getApplication<Application>().applicationContext

            val list = arrayListOf<Pair<Int, String>>()

            if (ConfigurationPreferences.isUsingRoot()) {
                if (checkIfAppIsLaunchable()) {
                    list.add(Pair(R.drawable.ic_launch, context.getString(R.string.launch)))
                }

                list.add(Pair(R.drawable.ic_send, context.getString(R.string.send)))
                list.add(Pair(R.drawable.ic_delete, context.getString(R.string.uninstall)))

                if (getApplication<Application>().packageManager.getApplicationInfo(applicationInfo.packageName, 0).enabled) {
                    list.add(Pair(R.drawable.ic_disable, context.getString(R.string.disable)))
                } else {
                    list.add(Pair(R.drawable.ic_check, context.getString(R.string.enable)))
                }

                list.add(Pair(R.drawable.ic_close, context.getString(R.string.force_stop)))
                list.add(Pair(R.drawable.ic_delete_sweep, context.getString(R.string.clear_data)))
                list.add(Pair(R.drawable.ic_broom, context.getString(R.string.clear_cache)))
                list.add(Pair(R.drawable.ic_double_arrow, context.getString(R.string.open_in_settings)))

            } else {
                if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    if (checkIfAppIsLaunchable()) {
                        list.add(Pair(R.drawable.ic_launch, context.getString(R.string.launch)))
                    }
                    list.add(Pair(R.drawable.ic_send, context.getString(R.string.send)))
                    list.add(Pair(R.drawable.ic_delete, context.getString(R.string.uninstall)))
                } else {
                    if (checkIfAppIsLaunchable()) {
                        list.add(Pair(R.drawable.ic_launch, context.getString(R.string.launch)))
                    }
                    list.add(Pair(R.drawable.ic_send, context.getString(R.string.send)))
                }

                list.add(Pair(R.drawable.ic_double_arrow, context.getString(R.string.open_in_settings)))
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
                Pair(R.drawable.ic_receivers, context.getString(R.string.receivers)),
                Pair(R.drawable.ic_provider, context.getString(R.string.providers)),
                Pair(R.drawable.ic_file_xml, context.getString(R.string.manifest)),
                Pair(R.drawable.ic_anchor, context.getString(R.string.uses_feature)),
                Pair(R.drawable.ic_graphics, context.getString(R.string.graphics)),
                Pair(R.drawable.ic_extras, context.getString(R.string.extras))
            )

            menuItems.postValue(list)
        }
    }

    private fun checkIfAppIsLaunchable(): Boolean {
        return getApplication<Application>().packageManager.getLaunchIntentForPackage(applicationInfo.packageName) != null
    }
}