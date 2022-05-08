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

class AppInfoMenuViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {
    private val menuItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadMetaOptions()
        }
    }

    private val menuOptions: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadActionOptions()
        }
    }

    private val miscellaneousItems: MutableLiveData<List<Pair<Int, Int>>> by lazy {
        MutableLiveData<List<Pair<Int, Int>>>().also {
            loadMiscellaneousItems()
        }
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getMenuItems(): LiveData<List<Pair<Int, Int>>> {
        return menuItems
    }

    fun getMenuOptions(): LiveData<List<Pair<Int, Int>>> {
        return menuOptions
    }

    fun getMiscellaneousItems(): LiveData<List<Pair<Int, Int>>> {
        return miscellaneousItems
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun loadActionOptions() {
        viewModelScope.launch(Dispatchers.Default) {
            if (!PackageUtils.isPackageInstalled(packageInfo.packageName, context.packageManager)) {
                error.postValue(context.getString(R.string.app_not_installed, packageInfo.packageName))
                return@launch
            }

            val list = arrayListOf<Pair<Int, Int>>()

            if (ConfigurationPreferences.isUsingRoot()) {
                if (PackageUtils.checkIfAppIsLaunchable(getApplication(), packageInfo.packageName) && isNotThisApp()) {
                    list.add(Pair(R.drawable.ic_launch, R.string.launch))
                }

                list.add(Pair(R.drawable.ic_send, R.string.send))

                if (isNotThisApp()) {
                    list.add(Pair(R.drawable.ic_delete, R.string.uninstall))

                    if (getApplication<Application>().packageManager.getApplicationInfo(packageInfo.packageName, 0).enabled) {
                        list.add(Pair(R.drawable.ic_disable, R.string.disable))
                    } else {
                        list.add(Pair(R.drawable.ic_check, R.string.enable))
                    }

                    list.add(Pair(R.drawable.ic_close, R.string.force_stop))
                    list.add(Pair(R.drawable.ic_delete_sweep, R.string.clear_data))
                    list.add(Pair(R.drawable.ic_broom, R.string.clear_cache))
                }

                list.add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))

            } else {
                if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                    if (PackageUtils.checkIfAppIsLaunchable(getApplication(), packageInfo.packageName) && isNotThisApp()) {
                        list.add(Pair(R.drawable.ic_launch, R.string.launch))
                    }

                    list.add(Pair(R.drawable.ic_send, R.string.send))

                    if (isNotThisApp()) {
                        list.add(Pair(R.drawable.ic_delete, R.string.uninstall))
                    }
                } else {
                    if (PackageUtils.checkIfAppIsLaunchable(getApplication(), packageInfo.packageName)) {
                        list.add(Pair(R.drawable.ic_launch, R.string.launch))
                    }
                    list.add(Pair(R.drawable.ic_send, R.string.send))
                }

                list.add(Pair(R.drawable.ic_double_arrow, R.string.open_in_settings))
            }

            menuOptions.postValue(list)
        }
    }

    fun loadMetaOptions() {
        viewModelScope.launch(Dispatchers.Default) {

            val list = listOf(
                    Pair(R.drawable.ic_permission, R.string.permissions),
                    Pair(R.drawable.ic_activities, R.string.activities),
                    Pair(R.drawable.ic_services, R.string.services),
                    Pair(R.drawable.ic_certificate, R.string.certificate),
                    Pair(R.drawable.ic_resources, R.string.resources),
                    Pair(R.drawable.ic_receivers, R.string.receivers),
                    Pair(R.drawable.ic_provider, R.string.providers),
                    Pair(R.drawable.ic_android, R.string.manifest),
                    Pair(R.drawable.ic_anchor, R.string.uses_feature),
                    Pair(R.drawable.ic_graphics, R.string.graphics),
                    Pair(R.drawable.ic_extras, R.string.extras),
                    Pair(R.drawable.ic_shared_libs, R.string.shared_libs),
                    Pair(R.drawable.ic_code, R.string.dex_classes),
                    Pair(R.drawable.ic_radiation_nuclear, R.string.trackers)
            )

            menuItems.postValue(list)
        }
    }

    fun loadMiscellaneousItems() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = arrayListOf<Pair<Int, Int>>()

            list.add(Pair(R.drawable.ic_downloading, R.string.extract))

            if (PackageUtils.isPackageInstalled("com.android.vending", getApplication<Application>().packageManager)) {
                list.add(Pair(R.drawable.ic_play_store, R.string.play_store))
            }

            if (PackageUtils.isPackageInstalled("com.amazon.venezia", getApplication<Application>().packageManager)) {
                list.add(Pair(R.drawable.ic_amazon, R.string.amazon))
            }

            if (PackageUtils.isPackageInstalled("org.fdroid.fdroid", getApplication<Application>().packageManager)) {
                list.add(Pair(R.drawable.ic_fdroid, R.string.fdroid))
            }

            miscellaneousItems.postValue(list)
        }
    }

    private fun isNotThisApp(): Boolean {
        return packageInfo.packageName != getApplication<Application>().packageName
    }
}