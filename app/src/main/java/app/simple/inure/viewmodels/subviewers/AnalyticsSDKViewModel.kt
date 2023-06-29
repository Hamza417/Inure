package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.util.SDKHelper
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsSDKViewModel(application: Application, private val entry: Entry) : PackageUtilsViewModel(application) {

    private val data1: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    private val data2: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    fun getMinimumSDKApps(): MutableLiveData<ArrayList<PackageInfo>> {
        return data1
    }

    fun getTargetSDKApps(): MutableLiveData<ArrayList<PackageInfo>> {
        return data2
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadSDKFilteredAppsList() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = getInstalledApps()
            val sdkFilteredApps = arrayListOf<PackageInfo>()

            for (app in apps) {
                if (AnalyticsPreferences.getSDKValue()) {
                    if (SDKHelper.getSdkCode(app.applicationInfo.minSdkVersion) == (entry as PieEntry).label) {
                        sdkFilteredApps.add(app)
                    }
                } else {
                    if (SDKHelper.getSdkTitle(app.applicationInfo.minSdkVersion) == (entry as PieEntry).label) {
                        sdkFilteredApps.add(app)
                    }
                }
            }

            for (app in sdkFilteredApps) {
                app.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageName)
            }

            sdkFilteredApps.sortBy {
                it.applicationInfo.name
            }

            data1.postValue(sdkFilteredApps)
        }
    }

    private fun loadTargetSDKFilteredAppsList() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = getInstalledApps()
            val sdkFilteredApps = arrayListOf<PackageInfo>()

            for (app in apps) {
                if (AnalyticsPreferences.getSDKValue()) {
                    if (SDKHelper.getSdkCode(app.applicationInfo.targetSdkVersion) == (entry as PieEntry).label) {
                        sdkFilteredApps.add(app)
                    }
                } else {
                    if (SDKHelper.getSdkTitle(app.applicationInfo.targetSdkVersion) == (entry as PieEntry).label) {
                        sdkFilteredApps.add(app)
                    }
                }
            }

            for (app in sdkFilteredApps) {
                app.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageName)
            }

            sdkFilteredApps.sortBy {
                it.applicationInfo.name
            }

            data2.postValue(sdkFilteredApps)
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            loadSDKFilteredAppsList()
        }
        loadTargetSDKFilteredAppsList()
    }
}