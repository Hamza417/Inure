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
import app.simple.inure.util.SDKHelper
import com.razerdp.widget.animatedpieview.AnimatedPieViewConfig
import com.razerdp.widget.animatedpieview.data.SimplePieInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsViewModel(application: Application) : WrappedViewModel(application) {

    private val minimumOsData: MutableLiveData<AnimatedPieViewConfig> by lazy {
        MutableLiveData<AnimatedPieViewConfig>().also {
            loadMinimumOsData()
        }
    }

    fun getMinimumOsData(): LiveData<AnimatedPieViewConfig> {
        return minimumOsData
    }

    private fun loadMinimumOsData() {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
            val data = arrayListOf<SimplePieInfo>()

            // TODO - improve this code
            for (sdkCode in 1..SDKHelper.totalSDKs) {
                var total = 0
                for (app in apps) {
                    val sdk = app.getMinSDK()
                    if (sdk == sdkCode) {
                        ++total
                    }
                }

                data.add(SimplePieInfo(total.toDouble(), SDKHelper.getSdkColor(sdkCode, getApplication()), SDKHelper.getSdkTitle(sdkCode)))
            }

            val animatedPieViewConfig = AnimatedPieViewConfig().apply {
                startAngle(-90.0F)
                addDatas(data)
            }

            minimumOsData.postValue(animatedPieViewConfig)
        }
    }

    private fun PackageInfo.getMinSDK(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            applicationInfo.minSdkVersion
        } else {
            applicationInfo.getApkMeta().minSdkVersion.toInt()
        }
    }
}