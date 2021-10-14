package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
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
        val context = getApplication<Application>().applicationContext
        val pi = context.packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_META_DATA)

        val version = PackageUtils.getApplicationVersion(context, packageInfo)
        val versionCode = PackageUtils.getApplicationVersionCode(context, packageInfo)

        val apex = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            if (pi.isApex)
                context.getString(R.string.yes) else context.getString(R.string.no)
        } else {
            context.getString(R.string.not_available)
        }

        val installLocation = kotlin.runCatching {
            when (pi.installLocation) {
                PackageInfo.INSTALL_LOCATION_AUTO -> context.getString(R.string.auto)
                PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY -> context.getString(R.string.internal)
                PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL -> context.getString(R.string.prefer_external)
                else -> {
                    if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                        context.getString(R.string.system)
                    } else {
                        context.getString(R.string.not_available)
                    }
                }
            }
        }.getOrElse {
            context.getString(R.string.not_available)
        }

        val glesVersion = kotlin.runCatching {
            if (packageInfo.getGlEsVersion().isEmpty()) {
                context.getString(R.string.not_available)
            } else {
                packageInfo.getGlEsVersion()
            }
        }.getOrElse {
            context.getString(R.string.not_available)
        }

        val uid = packageInfo.applicationInfo.uid.toString()
        val installDate = packageInfo.getApplicationInstallTime(context)
        val updateDate = packageInfo.getApplicationLastUpdateTime(context)

        val method = kotlin.runCatching {
            val p0 = packageInfo.applicationInfo.getDexData()!!
            var count = 0

            for (i in p0) {
                count = i.header.methodIdsSize
            }

            if (p0.size > 1) {
                String.format(context.getString(R.string.multi_dex), NumberFormat.getNumberInstance().format(count))
            } else {
                String.format(context.getString(R.string.single_dex), NumberFormat.getNumberInstance().format(count))
            }
        }.getOrElse {
            it.message!!
        }

        val minSdk = kotlin.runCatching {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                "${pi.applicationInfo.minSdkVersion}, ${SDKHelper.getSdkTitle(pi.applicationInfo.minSdkVersion)}"
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
            context.getString(R.string.dex_error)
        }

        val targetSdk = kotlin.runCatching {
            "${pi.applicationInfo.targetSdkVersion}, ${SDKHelper.getSdkTitle(pi.applicationInfo.targetSdkVersion)}"
        }.getOrElse {
            it.message!!
        }

        val applicationType = if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            context.getString(R.string.system)
        } else {
            context.getString(R.string.user)
        }

        val data = arrayListOf(
            Pair(context.getString(R.string.version), version.applySecondaryTextColor(context)),
            Pair(context.getString(R.string.version_code), versionCode.applySecondaryTextColor(context)),
            Pair(context.getString(R.string.install_location), installLocation.applySecondaryTextColor(context)),
            Pair(context.getString(R.string.gles_version), glesVersion.applySecondaryTextColor(context)),
            Pair(context.getString(R.string.uid), uid.applySecondaryTextColor(context)),
            Pair(context.getString(R.string.install_date), installDate.applyAccentColor()),
            Pair(context.getString(R.string.update_date), updateDate.applyAccentColor()),
            Pair(context.getString(R.string.minimum_sdk), minSdk.applyAccentColor()),
            Pair(context.getString(R.string.target_sdk), targetSdk.applyAccentColor()),
            Pair(context.getString(R.string.method_count), method.applySecondaryTextColor(context)),
            Pair(context.getString(R.string.apex), apex.applySecondaryTextColor(context)),
            Pair(context.getString(R.string.application_type), applicationType.applyAccentColor()),
        )

        information.postValue(data)
    }
}
