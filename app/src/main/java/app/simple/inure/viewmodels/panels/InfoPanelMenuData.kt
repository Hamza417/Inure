package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.preferences.ConfigurationPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class InfoPanelMenuData(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {
    private val menuItems: MutableLiveData<List<Pair<Int, String>>> by lazy {
        MutableLiveData<List<Pair<Int, String>>>().also {
            loadMetaOptions()
        }
    }

    private val menuOptions: MutableLiveData<List<Pair<Int, String>>> by lazy {
        MutableLiveData<List<Pair<Int, String>>>().also {
            loadActionOptions()
        }
    }

    private val miscellaneousItems: MutableLiveData<List<Pair<Int, String>>> by lazy {
        MutableLiveData<List<Pair<Int, String>>>().also {
            loadMiscellaneousItems()
        }
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getMenuItems(): LiveData<List<Pair<Int, String>>> {
        return menuItems
    }

    fun getMenuOptions(): LiveData<List<Pair<Int, String>>> {
        return menuOptions
    }

    fun getMiscellaneousItems(): LiveData<List<Pair<Int, String>>> {
        return miscellaneousItems
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun loadActionOptions() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = context

            if (!PackageUtils.isPackageInstalled(packageInfo.packageName, context.packageManager)) {
                error.postValue(context.getString(R.string.app_not_installed, packageInfo.packageName))
                return@launch
            }

            val list = arrayListOf<Pair<Int, String>>()

            if (ConfigurationPreferences.isUsingRoot()) {
                if (PackageUtils.checkIfAppIsLaunchable(getApplication(), packageInfo.packageName) && isNotThisApp()) {
                    list.add(Pair(R.drawable.ic_launch, context.getString(R.string.launch)))
                }

                list.add(Pair(R.drawable.ic_send, context.getString(R.string.send)))

                if (isNotThisApp()) {
                    list.add(Pair(R.drawable.ic_delete, context.getString(R.string.uninstall)))

                    if (getApplication<Application>().packageManager.getApplicationInfo(packageInfo.packageName, 0).enabled) {
                        list.add(Pair(R.drawable.ic_disable, context.getString(R.string.disable)))
                    } else {
                        list.add(Pair(R.drawable.ic_check, context.getString(R.string.enable)))
                    }

                    list.add(Pair(R.drawable.ic_close, context.getString(R.string.force_stop)))
                    list.add(Pair(R.drawable.ic_delete_sweep, context.getString(R.string.clear_data)))
                    list.add(Pair(R.drawable.ic_broom, context.getString(R.string.clear_cache)))
                }

                list.add(Pair(R.drawable.ic_double_arrow, context.getString(R.string.open_in_settings)))

            } else {
                if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    if (PackageUtils.checkIfAppIsLaunchable(getApplication(), packageInfo.packageName) && isNotThisApp()) {
                        list.add(Pair(R.drawable.ic_launch, context.getString(R.string.launch)))
                    }

                    list.add(Pair(R.drawable.ic_send, context.getString(R.string.send)))

                    if (isNotThisApp()) {
                        list.add(Pair(R.drawable.ic_delete, context.getString(R.string.uninstall)))
                    }
                } else {
                    if (PackageUtils.checkIfAppIsLaunchable(getApplication(), packageInfo.packageName)) {
                        list.add(Pair(R.drawable.ic_launch, context.getString(R.string.launch)))
                    }
                    list.add(Pair(R.drawable.ic_send, context.getString(R.string.send)))
                }

                list.add(Pair(R.drawable.ic_double_arrow, context.getString(R.string.open_in_settings)))
            }

            menuOptions.postValue(list)
        }
    }

    fun loadMetaOptions() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = context

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
                Pair(R.drawable.ic_extras, context.getString(R.string.extras)),
                Pair(R.drawable.ic_code, context.getString(R.string.dex_classes))
            )

            menuItems.postValue(list)
        }
    }

    fun loadMiscellaneousItems() {
        viewModelScope.launch(Dispatchers.Default) {
            val context = context

            val list = arrayListOf<Pair<Int, String>>()

            list.add(Pair(R.drawable.ic_backup, context.getString(R.string.extract)))

            if (PackageUtils.isPackageInstalled("com.android.vending", getApplication<Application>().packageManager)) {
                list.add(Pair(R.drawable.ic_play_store, context.getString(R.string.play_store)))
            }

            if (PackageUtils.isPackageInstalled("com.amazon.venezia", getApplication<Application>().packageManager)) {
                list.add(Pair(R.drawable.ic_amazon, context.getString(R.string.amazon)))
            }

            if (PackageUtils.isPackageInstalled("org.fdroid.fdroid", getApplication<Application>().packageManager)) {
                list.add(Pair(R.drawable.ic_fdroid, context.getString(R.string.fdroid)))
            }

            miscellaneousItems.postValue(list)
        }
    }

    private fun isNotThisApp(): Boolean {
        return packageInfo.packageName != getApplication<Application>().packageName
    }
}