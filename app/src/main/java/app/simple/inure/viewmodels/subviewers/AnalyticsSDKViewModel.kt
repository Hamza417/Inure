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

    private val data: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                loadSDKFilteredAppsList()
            }
        }
    }

    fun getPackageData(): MutableLiveData<ArrayList<PackageInfo>> {
        return data
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

            data.postValue(sdkFilteredApps)
        }
    }
}