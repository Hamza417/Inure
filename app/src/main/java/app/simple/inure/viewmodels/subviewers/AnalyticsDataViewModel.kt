package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.util.SDKHelper
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AnalyticsDataViewModel(application: Application, private val entry: Entry) : PackageUtilsViewModel(application) {

    private val data: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    fun getAnalyticsData(): MutableLiveData<ArrayList<PackageInfo>> {
        return data
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadSDKFilteredAppsList(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(250L) // Optimization baybeee!!!

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

            data.postValue(sdkFilteredApps)
        }
    }

    private fun loadTargetSDKFilteredAppsList(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(250L) // Optimization baybeee!!!

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

            data.postValue(sdkFilteredApps)
        }
    }

    private fun loadSplitPackageAppsList(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(250L) // Optimization baybeee!!!

            val packageApps = arrayListOf<PackageInfo>()

            val splitApkString = getString(R.string.split_packages)
            val apk = getString(R.string.apk)

            for (app in apps) {
                if ((entry as PieEntry).label == splitApkString) {
                    if (app.applicationInfo.splitSourceDirs?.isNotEmpty() == true) {
                        packageApps.add(app)
                    }
                } else if (entry.label == apk) {
                    if (app.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                        packageApps.add(app)
                    }
                }
            }

            for (app in packageApps) {
                app.applicationInfo.name = PackageUtils.getApplicationName(applicationContext(), app.packageName)
            }

            packageApps.sortBy {
                it.applicationInfo.name
            }

            data.postValue(packageApps)
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            loadSDKFilteredAppsList(apps)
        }

        loadTargetSDKFilteredAppsList(apps)
        loadSplitPackageAppsList(apps)
    }
}