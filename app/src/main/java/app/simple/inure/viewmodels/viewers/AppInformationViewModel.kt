package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.parsers.APKParser.getApkMeta
import app.simple.inure.apk.parsers.APKParser.getDexData
import app.simple.inure.apk.parsers.APKParser.getGlEsVersion
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInstallTime
import app.simple.inure.apk.utils.PackageUtils.getApplicationLastUpdateTime
import app.simple.inure.util.SDKHelper
import com.jaredrummler.apkparser.model.ApkMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat

class AppInformationViewModel(application: Application, val applicationInfo: ApplicationInfo) : AndroidViewModel(application) {

    private val information: MutableLiveData<ArrayList<Pair<String, String>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, String>>>().also {
            viewModelScope.launch(Dispatchers.IO) {
                loadInformation()
            }
        }
    }

    private val progress: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    fun getInformation(): LiveData<ArrayList<Pair<String, String>>> {
        return information
    }

    fun getProgress(): LiveData<Int> {
        return progress
    }

    private fun loadInformation() {
        val context = getApplication<Application>().applicationContext
        val pi = context.packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_META_DATA)

        progress.postValue(0)

        val version = PackageUtils.getApplicationVersion(context, applicationInfo)
        val versionCode = PackageUtils.getApplicationVersionCode(context, applicationInfo)

        progress.postValue(11)

        val apex = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (pi.isApex)
                context.getString(R.string.yes) else context.getString(R.string.no)
        } else {
            context.getString(R.string.not_available)
        }

        progress.postValue(22)

        val installLocation = kotlin.runCatching {
            when (pi.installLocation) {
                PackageInfo.INSTALL_LOCATION_AUTO -> context.getString(R.string.auto)
                PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY -> context.getString(R.string.internal)
                PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL -> context.getString(R.string.prefer_external)
                else -> {
                    if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                        context.getString(R.string.system)
                    } else {
                        context.getString(R.string.not_available)
                    }
                }
            }
        }.getOrElse {
            context.getString(R.string.not_available)
        }

        progress.postValue(33)

        val glesVersion = kotlin.runCatching {
            if (applicationInfo.getGlEsVersion().isEmpty()) {
                context.getString(R.string.not_available)
            } else {
                applicationInfo.getGlEsVersion()
            }
        }.getOrElse {
            it.message!!
        }

        progress.postValue(44)

        val uid = applicationInfo.uid.toString()
        val installDate = applicationInfo.getApplicationInstallTime(context)
        val updateDate = applicationInfo.getApplicationLastUpdateTime(context)

        progress.postValue(55)

        val v = applicationInfo.getDexData()!!
        var methodCount = 0L
        for (i in v) {
            methodCount += i.header.methodIdsSize
        }

        val isMultiDex = v.size > 1

        val method = if (isMultiDex) {
            String.format(context.getString(R.string.multi_dex), NumberFormat.getNumberInstance().format(methodCount))
        } else {
            String.format(context.getString(R.string.single_dex), NumberFormat.getNumberInstance().format(methodCount))
        }

        progress.postValue(66)

        val minSdk = kotlin.runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                "${pi.applicationInfo.minSdkVersion}, ${SDKHelper.getSdkTitle(pi.applicationInfo.minSdkVersion)}"
            } else {
                when (val apkMeta: Any? = applicationInfo.getApkMeta()) {
                    is ApkMeta -> {
                        "${apkMeta.minSdkVersion}, ${SDKHelper.getSdkTitle(apkMeta.minSdkVersion)}"
                    }
                    is net.dongliu.apk.parser.bean.ApkMeta -> {
                        "${apkMeta.minSdkVersion}, ${SDKHelper.getSdkTitle(apkMeta.minSdkVersion)}"
                    }
                    else -> {
                        getApplication<Application>().getString(R.string.not_available)
                    }
                }
            }
        }.getOrElse {
            it.message!!
        }

        progress.postValue(77)

        val targetSdk = kotlin.runCatching {
            "${pi.applicationInfo.targetSdkVersion}, ${SDKHelper.getSdkTitle(pi.applicationInfo.targetSdkVersion)}"
        }.getOrElse {
            it.message!!
        }

        progress.postValue(88)

        val applicationType = if (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            context.getString(R.string.system)
        } else {
            context.getString(R.string.user)
        }

        progress.postValue(99)

        val data = arrayListOf(
            Pair(context.getString(R.string.version), version),
            Pair(context.getString(R.string.version_code), versionCode),
            Pair(context.getString(R.string.install_location), installLocation),
            Pair(context.getString(R.string.gles_version), glesVersion),
            Pair(context.getString(R.string.uid), uid),
            Pair(context.getString(R.string.install_date), installDate),
            Pair(context.getString(R.string.update_date), updateDate),
            Pair(context.getString(R.string.minimum_sdk), minSdk),
            Pair(context.getString(R.string.target_sdk), targetSdk),
            Pair(context.getString(R.string.method_count), method),
            Pair(context.getString(R.string.apex), apex),
            Pair(context.getString(R.string.application_type), applicationType),
        )

        information.postValue(data)
    }
}