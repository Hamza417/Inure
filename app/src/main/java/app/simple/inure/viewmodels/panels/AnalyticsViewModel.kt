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

    private val installLocationData: MutableLiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> by lazy {
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

    fun getInstallLocationData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return installLocationData
    }

    fun getPackageTypeData(): LiveData<Pair<ArrayList<PieEntry>, ArrayList<Int>>> {
        return packageTypeData
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadMinimumOsData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
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
                    colors.add(SDKHelper.getSdkColor(sdkCode, applicationContext()))
                }
            }

            targetOsData.postValue(Pair(data, colors))
        }
    }

    private fun loadInstallLocationData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
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

    private fun loadPackageTypeData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()

            var split = 0F
            var apk = 0F

            for (app in apps) {
                if (app.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                    apk++
                } else {
                    split++
                }
            }

            if (split != 0F) data.add(PieEntry(split, getString(R.string.split_packages)))
            if (apk != 0F) data.add(PieEntry(apk, getString(R.string.apk)))

            packageTypeData.postValue(Pair(data, colors))
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            loadMinimumOsData(apps)
        }
        loadTargetOsData(apps)
        loadInstallLocationData(apps)
        loadPackageTypeData(apps)
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
    }
}