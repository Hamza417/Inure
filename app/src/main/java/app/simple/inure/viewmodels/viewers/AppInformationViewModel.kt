package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.text.Spannable
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
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import com.jaredrummler.apkparser.model.ApkMeta
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.NumberFormat

class AppInformationViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val information: MutableLiveData<ArrayList<Pair<String, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, Spannable>>>().also {
            viewModelScope.launch(Dispatchers.IO) {
                loadInformation()
            }
        }
    }

    fun getInformation(): LiveData<ArrayList<Pair<String, Spannable>>> {
        return information
    }

    private fun loadInformation() {
        information.postValue(arrayListOf(
            getVersion(),
            getVersionCode(),
            getInstallLocation(),
            getGlesVersion(),
            getUID(),
            getInstallDate(),
            getUpdateDate(),
            getMinSDK(),
            getTargetSDK(),
            getMethodCount(),
            getApex(),
            getApplicationType(),
        ))
    }

    private fun getVersion(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.version),
                    PackageUtils.getApplicationVersion(getApplication(), packageInfo).applySecondaryTextColor(getApplication()))
    }

    private fun getVersionCode(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.version),
                    PackageUtils.getApplicationVersionCode(getApplication(), packageInfo).applySecondaryTextColor(getApplication()))
    }

    private fun getInstallLocation(): Pair<String, Spannable> {
        val installLocation = kotlin.runCatching {
            when (packageInfo.installLocation) {
                PackageInfo.INSTALL_LOCATION_AUTO -> getApplication<Application>().getString(R.string.auto)
                PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY -> getApplication<Application>().getString(R.string.internal)
                PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL -> getApplication<Application>().getString(R.string.prefer_external)
                else -> {
                    if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                        getApplication<Application>().getString(R.string.system)
                    } else {
                        getApplication<Application>().getString(R.string.not_available)
                    }
                }
            }
        }.getOrElse {
            getApplication<Application>().getString(R.string.not_available)
        }

        return Pair(getApplication<Application>().getString(R.string.install_location),
                    installLocation.applySecondaryTextColor(getApplication()))
    }

    private fun getGlesVersion(): Pair<String, Spannable> {
        val glesVersion = kotlin.runCatching {
            if (packageInfo.getGlEsVersion().isEmpty()) {
                getApplication<Application>().getString(R.string.not_available)
            } else {
                packageInfo.getGlEsVersion()
            }
        }.getOrElse {
            getApplication<Application>().getString(R.string.not_available)
        }

        return Pair(getApplication<Application>().getString(R.string.gles_version),
                    glesVersion.applySecondaryTextColor(getApplication()))
    }

    private fun getUID(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.uid),
                    packageInfo.applicationInfo.uid.toString().applySecondaryTextColor(getApplication()))
    }

    private fun getInstallDate(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.install_date),
                    packageInfo.getApplicationInstallTime(getApplication()).applyAccentColor())
    }

    private fun getUpdateDate(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.update_date),
                    packageInfo.getApplicationLastUpdateTime(getApplication()).applyAccentColor())
    }

    private fun getMinSDK(): Pair<String, Spannable> {
        val minSdk = kotlin.runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                "${packageInfo.applicationInfo.minSdkVersion}, ${SDKHelper.getSdkTitle(packageInfo.applicationInfo.minSdkVersion)}"
            } else {
                when (val apkMeta: Any? = packageInfo.applicationInfo.getApkMeta()) {
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
            getApplication<Application>().getString(R.string.not_available)
        }

        return Pair(getApplication<Application>().getString(R.string.minimum_sdk),
                    minSdk.applyAccentColor())
    }

    private fun getTargetSDK(): Pair<String, Spannable> {
        val targetSdk = kotlin.runCatching {
            "${packageInfo.applicationInfo.targetSdkVersion}, ${SDKHelper.getSdkTitle(packageInfo.applicationInfo.targetSdkVersion)}"
        }.getOrElse {
            it.message!!
        }

        return Pair(getApplication<Application>().getString(R.string.target_sdk),
                    targetSdk.applyAccentColor())
    }

    private fun getMethodCount(): Pair<String, Spannable> {
        val method = kotlin.runCatching {
            val p0 = packageInfo.applicationInfo.getDexData()!!
            var count = 0

            for (i in p0) {
                count = i.header.methodIdsSize
            }

            if (p0.size > 1) {
                String.format(getApplication<Application>().getString(R.string.multi_dex), NumberFormat.getNumberInstance().format(count))
            } else {
                String.format(getApplication<Application>().getString(R.string.single_dex), NumberFormat.getNumberInstance().format(count))
            }
        }.getOrElse {
            it.message!!
        }

        return Pair(getApplication<Application>().getString(R.string.method_count),
                    method.applySecondaryTextColor(getApplication()))
    }

    private fun getApex(): Pair<String, Spannable> {
        val apex = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (packageInfo.isApex)
                getApplication<Application>().getString(R.string.yes) else getApplication<Application>().getString(R.string.no)
        } else {
            getApplication<Application>().getString(R.string.not_available)
        }

        return Pair(getApplication<Application>().getString(R.string.apex),
                    apex.applySecondaryTextColor(getApplication()))
    }

    private fun getApplicationType(): Pair<String, Spannable> {
        val applicationType = if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            getApplication<Application>().getString(R.string.system)
        } else {
            getApplication<Application>().getString(R.string.user)
        }

        return Pair(getApplication<Application>().getString(R.string.application_type),
                    applicationType.applySecondaryTextColor(getApplication()))
    }
}
