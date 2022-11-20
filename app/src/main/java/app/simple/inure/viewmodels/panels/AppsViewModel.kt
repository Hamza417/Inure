package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getApplicationName
import app.simple.inure.constants.Misc
import app.simple.inure.events.AppsEvent
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.util.stream.Collectors

class AppsViewModel(application: Application) : WrappedViewModel(application) {

    private val appData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            loadAppData()
        }
    }

    private val generatedAppDataPath: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    val appLoaded: MutableLiveData<AppsEvent<Boolean>> by lazy {
        MutableLiveData<AppsEvent<Boolean>>()
    }

    fun getAppData(): LiveData<ArrayList<PackageInfo>> {
        return appData
    }

    fun getGeneratedAppData(): LiveData<String> {
        return generatedAppDataPath
    }

    fun loadAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getInstalledPackages(PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            }

            when (MainPreferences.getAppsCategory()) {
                PopupAppsCategory.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                PopupAppsCategory.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            for (i in apps.indices) {
                apps[i].applicationInfo.name = getApplicationName(getApplication<Application>().applicationContext, apps[i].applicationInfo)
            }

            apps.getSortedList(MainPreferences.getSortStyle(), MainPreferences.isReverseSorting())

            appData.postValue(apps as ArrayList<PackageInfo>?)
            appLoaded.postValue(AppsEvent(true))
        }
    }

    fun generateAllAppsTXTFile() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(Misc.delay)
            val apps = appData.value

            val path = applicationContext().cacheDir.absolutePath + "/all_apps_generated_data.txt"

            if (File(path).exists()) {
                if (File(path).delete()) {
                    Log.d("AppsViewModel", "Deleted old generated file")
                }
            }

            if (apps != null) {
                val stringBuilder = StringBuilder()

                stringBuilder.append("Total apps: ${apps.size}")

                for (i in apps.indices) {
                    stringBuilder.append("\n\n")
                    stringBuilder.append(apps[i].applicationInfo.packageName)
                    stringBuilder.append("\n\t")
                    stringBuilder.append("Name: ${apps[i].applicationInfo.name}")
                    stringBuilder.append("\n\t")
                    stringBuilder.append("Version: ${apps[i].versionName}")
                    stringBuilder.append("\n\t")
                    stringBuilder.append("Installed on: ${apps[i].firstInstallTime.toDate()}")
                    stringBuilder.append("\n\t")
                    stringBuilder.append("Updated on: ${apps[i].lastUpdateTime.toDate()}")
                }

                stringBuilder.append("\n")

                FileOutputStream(path).use { fileOutputStream ->
                    OutputStreamWriter(fileOutputStream).use {
                        it.write(stringBuilder.toString()) // skip the formatting here, the viewer should format the XML files
                    }
                }

                generatedAppDataPath.postValue(path)
            } else {
                postWarning(getString(R.string.not_available))
            }
        }
    }

    fun clearGeneratedAppsDataLiveData() {
        generatedAppDataPath.postValue(null)
    }
}