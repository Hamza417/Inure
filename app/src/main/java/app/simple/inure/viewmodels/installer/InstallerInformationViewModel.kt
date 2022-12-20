package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import android.text.Spannable
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.parsers.APKParser.getApkArchitecture
import app.simple.inure.apk.parsers.APKParser.getNativeLibraries
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInstallTime
import app.simple.inure.apk.utils.PackageUtils.getApplicationLastUpdateTime
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.SDKHelper
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import java.io.File
import java.text.NumberFormat

class InstallerInformationViewModel(application: Application, private val file: File) : WrappedViewModel(application) {

    private var apkFile: ApkFile? = null
    private var packageInfo: PackageInfo? = null

    private val information: MutableLiveData<ArrayList<Pair<Int, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<@StringRes Int, Spannable>>>().also {
            viewModelScope.launch(Dispatchers.IO) {
                loadInformation()
            }
        }
    }

    fun getInformation(): LiveData<ArrayList<Pair<Int, Spannable>>> {
        return information
    }

    private fun loadInformation() {
        apkFile = ApkFile(file)

        if (packageManager.isPackageInstalled(apkFile!!.apkMeta.packageName)) {
            packageInfo = packageManager.getPackageInfo(apkFile!!.apkMeta.packageName)
        }

        val list = arrayListOf<Pair<Int, Spannable>>()

        list.add(getPackageName())
        list.add(getVersion())
        list.add(getVersionCode())

        if (packageInfo.isNotNull()) {
            list.add(getUID())
            list.add(getInstallDate())
            list.add(getUpdateDate())
            list.add(getInstallerName())
        }

        list.add(getMinSDK())
        list.add(getTargetSDK())
        list.add(getGlesVersion())
        list.add(getArchitecture())
        list.add(getNativeLibraries())
        list.add(getMethodCount())
        list.add(getFeatures())
        list.add(getApkPath())

        information.postValue(list)
    }

    private fun getPackageName(): Pair<Int, Spannable> {
        return Pair(R.string.package_name,
                    apkFile!!.apkMeta.packageName.applySecondaryTextColor())
    }

    private fun getVersion(): Pair<Int, Spannable> {
        return Pair(R.string.version,
                    apkFile?.apkMeta?.versionName?.applySecondaryTextColor() ?: "N/A".applySecondaryTextColor())
    }

    private fun getVersionCode(): Pair<Int, Spannable> {
        return Pair(R.string.version_code,
                    apkFile!!.apkMeta.versionCode.toString().applySecondaryTextColor())
    }

    private fun getApkPath(): Pair<Int, Spannable> {
        return Pair(R.string.apk_base_package,
                    file.path.applySecondaryTextColor())
    }

    private fun getGlesVersion(): Pair<Int, Spannable> {
        val glesVersion = kotlin.runCatching {
            apkFile!!.apkMeta.glEsVersion.toString().ifEmpty { getString(R.string.not_available) }
        }.getOrElse {
            getString(R.string.not_available)
        }

        return Pair(R.string.gles_version,
                    glesVersion.applySecondaryTextColor())
    }

    private fun getArchitecture(): Pair<Int, Spannable> {
        return Pair(R.string.architecture,
                    file.getApkArchitecture(context).toString().applyAccentColor())
    }

    private fun getNativeLibraries(): Pair<Int, Spannable> {
        return Pair(R.string.native_libraries,
                    file.getNativeLibraries(context).toString().applySecondaryTextColor())
    }

    private fun getUID(): Pair<Int, Spannable> {
        return Pair(R.string.uid,
                    packageInfo!!.applicationInfo.uid.toString().applySecondaryTextColor())
    }

    private fun getInstallDate(): Pair<Int, Spannable> {
        return Pair(R.string.install_date,
                    packageInfo!!.getApplicationInstallTime(context, FormattingPreferences.getDateFormat()).applyAccentColor())
    }

    private fun getUpdateDate(): Pair<Int, Spannable> {
        return Pair(R.string.update_date,
                    packageInfo!!.getApplicationLastUpdateTime(context, FormattingPreferences.getDateFormat()).applyAccentColor())
    }

    private fun getMinSDK(): Pair<Int, Spannable> {
        val minSdk = kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                "${apkFile!!.apkMeta.minSdkVersion}, ${SDKHelper.getSdkTitle(apkFile!!.apkMeta.minSdkVersion)}"
            } else {
                when (val apkMeta: Any = apkFile!!.apkMeta) {
                    is ApkMeta -> {
                        "${apkMeta.minSdkVersion}, ${SDKHelper.getSdkTitle(apkMeta.minSdkVersion)}"
                    }
                    else -> {
                        getString(R.string.not_available)
                    }
                }
            }
        }.getOrElse {
            getString(R.string.not_available)
        }

        return Pair(R.string.minimum_sdk,
                    minSdk.applyAccentColor())
    }

    private fun getTargetSDK(): Pair<Int, Spannable> {
        val targetSdk = kotlin.runCatching {
            "${apkFile!!.apkMeta.targetSdkVersion}, ${SDKHelper.getSdkTitle(apkFile!!.apkMeta.targetSdkVersion)}"
        }.getOrElse {
            it.message!!
        }

        return Pair(R.string.target_sdk,
                    targetSdk.applyAccentColor())
    }

    private fun getMethodCount(): Pair<Int, Spannable> {
        val method = kotlin.runCatching {
            val p0 = apkFile!!.dexClasses
            var count = 0

            for (i in p0) {
                count = i.javaClass.methods.size
            }

            if (p0.size > 1) {
                String.format(getString(R.string.multi_dex), NumberFormat.getNumberInstance().format(count))
            } else {
                String.format(getString(R.string.single_dex), NumberFormat.getNumberInstance().format(count))
            }
        }.getOrElse {
            it.message!!
        }

        return Pair(R.string.method_count,
                    method.applySecondaryTextColor())
    }

    private fun getInstallerName(): Pair<Int, Spannable> {
        @Suppress("deprecation")
        val name = kotlin.runCatching {
            val p0 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageInfo!!.packageName).installingPackageName
            } else {
                packageManager.getInstallerPackageName(packageInfo!!.packageName)
            }

            PackageUtils.getApplicationName(context, p0!!)
        }.getOrElse {
            getString(R.string.not_available)
        }

        return Pair(R.string.installer,
                    name!!.applySecondaryTextColor())
    }

    private fun getFeatures(): Pair<Int, Spannable> {
        val features = StringBuilder()

        try {
            for (feature in apkFile!!.apkMeta.usesFeatures) {
                if (features.isEmpty()) {
                    features.append(feature.name)
                } else {
                    features.append("\n")
                    features.append(feature.name)
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            features.append(getString(R.string.not_available))
        } finally {
            if (features.isEmpty()) {
                features.append(getString(R.string.not_available))
            }
        }

        return Pair(R.string.uses_feature,
                    features.toString().applySecondaryTextColor())
    }
}