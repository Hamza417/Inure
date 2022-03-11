package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser.getApkMeta
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.util.SDKHelper
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : WrappedViewModel(application) {

    private val minimumOsData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
        MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>>().also {
            loadMinimumOsData()
        }
    }

    private val targetOsData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
        MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>>().also {
            loadTargetOsData()
        }
    }

    fun getMinimumOsData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return minimumOsData
    }

    fun getTargetOsData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return targetOsData
    }

    private fun loadMinimumOsData() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()

            // TODO - improve this code
            for (sdkCode in 1..SDKHelper.totalSDKs) {
                var total = 0F
                for (app in apps) {
                    val sdk = app.getMinSDK()
                    if (sdk == sdkCode) {
                        ++total
                    }
                }

                if (total != 0F) { // Filter empty data
                    data.add(PieEntry(total, if (AnalyticsPreferences.getSDKValue()) SDKHelper.getSdkCode(sdkCode) else SDKHelper.getSdkTitle(sdkCode)))
                    colors.add(SDKHelper.getSdkColor(sdkCode, getApplication()))
                }
            }

            minimumOsData.postValue(Pair(data, colors))
        }
    }

    private fun loadTargetOsData() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()

            // TODO - improve this code
            for (sdkCode in 1..SDKHelper.totalSDKs) {
                var total = 0F
                for (app in apps) {
                    val sdk = app.applicationInfo.targetSdkVersion
                    if (sdk == sdkCode) {
                        ++total
                    }
                }

                if (total != 0F) { // Filter empty data
                    data.add(PieEntry(total, if (AnalyticsPreferences.getSDKValue()) SDKHelper.getSdkCode(sdkCode) else SDKHelper.getSdkTitle(sdkCode)))
                    colors.add(SDKHelper.getSdkColor(sdkCode, getApplication()))
                }
            }

            targetOsData.postValue(Pair(data, colors))
        }
    }

    private fun PackageInfo.getMinSDK(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationInfo.minSdkVersion
        } else {
            applicationInfo.getApkMeta().minSdkVersion.toInt()
        }
    }

    internal fun refresh() {
        loadMinimumOsData()
        loadTargetOsData()
    }
}