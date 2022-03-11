package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.util.SDKHelper
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : WrappedViewModel(application) {

    private val minimumOsData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
        MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>>().also {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                loadMinimumOsData()
            }
        }
    }

    private val targetOsData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
        MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>>().also {
            loadTargetOsData()
        }
    }

    private val installLocationData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
        MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>>().also {
            loadInstallLocationData()
        }
    }

    fun getMinimumOsData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return minimumOsData
    }

    fun getTargetOsData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return targetOsData
    }

    fun getInstallLocationData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return installLocationData
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadMinimumOsData() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()

            // TODO - improve this code
            for (sdkCode in 1..SDKHelper.totalSDKs) {
                var total = 0F
                for (app in apps) {
                    val sdk = app.applicationInfo.minSdkVersion
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

    private fun loadInstallLocationData() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()

            var internal = 0F
            var external = 0F
            var auto = 0F
            var unspecified = 0F

            for (app in apps) {
                when (app.installLocation) {
                    PackageInfo.INSTALL_LOCATION_AUTO -> auto++
                    PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY -> internal++
                    PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL -> external++
                    -1 -> unspecified++
                }
            }

            if (internal != 0F) data.add(PieEntry(internal, getString(R.string.internal)))
            if (external != 0F) data.add(PieEntry(external, getString(R.string.prefer_external)))
            if (auto != 0F) data.add(PieEntry(auto, getString(R.string.auto)))
            if (unspecified != 0F) data.add(PieEntry(unspecified, getString(R.string.unspecified)))

            installLocationData.postValue(Pair(data, colors))
        }
    }

    internal fun refresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            loadMinimumOsData()
        }
        loadTargetOsData()
    }
}