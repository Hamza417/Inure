package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getApplicationName
import app.simple.inure.events.AppsEvent
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort.getSortedList
import app.simple.inure.util.XMLUtils.formatXML
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    fun generateAllAppsXMLFile() {
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000)
            val apps = appData.value
            val path = applicationContext().cacheDir.absolutePath + "/all_apps_generated_data.xml"

            if (apps != null) {
                val stringBuilder = StringBuilder()

                stringBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
                stringBuilder.append("<resources>\n")

                for (i in apps.indices) {
                    // XML Array
                    stringBuilder.append("\t<string-array name=\"${apps[i].packageName}\">\n")
                    stringBuilder.append("\t\t<item>${apps[i].applicationInfo.name}</item>\n")
                    stringBuilder.append("\t\t<item>${apps[i].packageName}</item>\n")
                    stringBuilder.append("\t\t<item>${apps[i].versionName}</item>\n")
                    stringBuilder.append("\t</string-array>\n")
                    stringBuilder.append("\n")
                }

                stringBuilder.append("</resources>")

                FileOutputStream(path).use { fileOutputStream ->
                    OutputStreamWriter(fileOutputStream).use {
                        it.write(stringBuilder.toString().formatXML())
                    }
                }

                generatedAppDataPath.postValue(path)
            } else {
                postWarning(getString(R.string.not_available))
            }
        }
    }
}