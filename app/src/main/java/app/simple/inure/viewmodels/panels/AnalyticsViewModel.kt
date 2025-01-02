package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getInstallerPackageName
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.constants.Colors
import app.simple.inure.constants.InstallerColors
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.preferences.AnalyticsPreferences
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.SDKUtils
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

    private val installerData: MutableLiveData<Triple<ArrayList<PieEntry>, ArrayList<Int>, HashMap<String, String>>> by lazy {
        MutableLiveData<Triple<ArrayList<PieEntry>, ArrayList<Int>, HashMap<String, String>>>()
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

    fun getInstallerData(): LiveData<Triple<ArrayList<PieEntry>, ArrayList<Int>, HashMap<String, String>>> {
        return installerData
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun loadMinimumOsData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()
            val isSdkCode = AnalyticsPreferences.getSDKValue()

            for (sdkCode in 1..SDKUtils.TOTAL_SDKS) {
                var total = 0F

                for (app in apps) {
                    val sdk = app.safeApplicationInfo.minSdkVersion
                    if (sdk == sdkCode) {
                        ++total
                    }
                }

                if (total.isNotZero()) { // Filter empty data
                    val sdk = if (isSdkCode) SDKUtils.getSdkCode(sdkCode) else SDKUtils.getSdkTitle(sdkCode)

                    data.add(PieEntry(total, sdk))
                    colors.add(SDKUtils.getSdkColor(sdkCode, applicationContext()))
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

            for (sdkCode in 1..SDKUtils.TOTAL_SDKS) {
                var total = 0F

                for (app in apps) {
                    val sdk = app.safeApplicationInfo.targetSdkVersion
                    if (sdk == sdkCode) {
                        total = total.inc()
                    }
                }

                if (total.isNotZero()) { // Filter empty data
                    val sdk = if (isSdkCode) SDKUtils.getSdkCode(sdkCode) else SDKUtils.getSdkTitle(sdkCode)

                    data.add(PieEntry(total, sdk))
                    colors.add(SDKUtils.getSdkColor(sdkCode, applicationContext()))
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
                if (app.safeApplicationInfo.splitSourceDirs.isNullOrEmpty()) {
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

    private fun loadInstallerData(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.Default) {
            val data = arrayListOf<PieEntry>()
            val colors = arrayListOf<Int>()
            val installers = hashMapOf<String, Int>()
            val labels = hashMapOf<String, String>()

            for (app in apps) {
                val installer = app.getInstallerPackageName(application.applicationContext)

                if (installer != null) {
                    if (installers.containsKey(installer)) {
                        installers[installer] = installers[installer]!!.inc()
                    } else {
                        installers[installer] = 1
                    }
                } else {
                    if (installers.containsKey(getString(R.string.unknown))) {
                        installers[getString(R.string.unknown)] = installers[getString(R.string.unknown)]!!.inc()
                    } else {
                        installers[getString(R.string.unknown)] = 1
                    }
                }
            }

            for (installer in installers) {
                data.add(PieEntry(installer.value.toFloat(), installer.key))
            }

            installers.keys.distinct().forEach { packageName ->
                colors.add(InstallerColors.getInstallerColorMap()[packageName]
                               ?: Colors.getRetroColor()[installers.keys.distinct().indexOf(packageName)])
                labels[packageName] = kotlin.runCatching {
                    packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName)!!).toString()
                }.getOrElse {
                    packageName
                }
            }

            installerData.postValue(Triple(data, colors, labels))
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            loadMinimumOsData(apps)
        }

        loadTargetOsData(apps)
        loadPackageTypeData(apps)
        loadInstallerData(apps)
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
    }
}
