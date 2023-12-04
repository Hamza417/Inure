package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.SDKHelper
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : PackageUtilsViewModel(application) {

    private val minimumOsData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
        MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>>()
    }

    private val targetOsData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
        MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>>()
    }

    private val packageTypeData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
        MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>>()
    }

    fun getMinimumOsData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return minimumOsData
    }

    fun getTargetSDKData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return targetOsData
    }

    fun getPackageTypeData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return packageTypeData
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadMinimumOsData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()
            val isSdkCode = AnalyticsPreferences.getSDKValue()

            for (sdkCode in 1..SDKHelper.totalSDKs) {
                var total = 0F

                for (app in apps) {
                    val sdk = app.applicationInfo.minSdkVersion
                    if (sdk == sdkCode) {
                        ++total
                    }
                }

                if (total.isNotZero()) { // Filter empty data
                    val sdk = if (isSdkCode) SDKHelper.getSdkCode(sdkCode) else SDKHelper.getSdkTitle(sdkCode)

                    data.add(PieEntry(total, sdk))
                    colors.add(SDKHelper.getSdkColor(sdkCode, applicationContext()))
                }
            }

            minimumOsData.postValue(Pair(data, colors))
        }
    }

    private fun loadTargetOsData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()
            val isSdkCode = AnalyticsPreferences.getSDKValue()

            for (sdkCode in 1..SDKHelper.totalSDKs) {
                var total = 0F

                for (app in apps) {
                    val sdk = app.applicationInfo.targetSdkVersion
                    if (sdk == sdkCode) {
                        total = total.inc()
                    }
                }

                if (total.isNotZero()) { // Filter empty data
                    val sdk = if (isSdkCode) SDKHelper.getSdkCode(sdkCode) else SDKHelper.getSdkTitle(sdkCode)

                    data.add(PieEntry(total, sdk))
                    colors.add(SDKHelper.getSdkColor(sdkCode, applicationContext()))
                }
            }

            targetOsData.postValue(Pair(data, colors))
        }
    }

    private fun loadPackageTypeData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()
            var split = 0F
            var apk = 0F

            for (app in apps) {
                if (app.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    apk = apk.inc()
                } else {
                    split = split.inc()
                }
            }

            if (split.isNotZero()) data.add(PieEntry(split, getString(R.string.split_packages)))
            if (apk.isNotZero()) data.add(PieEntry(apk, getString(R.string.apk)))

            packageTypeData.postValue(Pair(data, colors))
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            loadMinimumOsData(apps)
        }

        loadTargetOsData(apps)
        loadPackageTypeData(apps)
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
    }
}