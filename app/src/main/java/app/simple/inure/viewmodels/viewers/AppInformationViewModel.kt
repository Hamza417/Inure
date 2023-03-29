package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.text.Spannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.parsers.APKParser.getApkArchitecture
import app.simple.inure.apk.parsers.APKParser.getApkMeta
import app.simple.inure.apk.parsers.APKParser.getDexData
import app.simple.inure.apk.parsers.APKParser.getGlEsVersion
import app.simple.inure.apk.parsers.APKParser.getNativeLibraries
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInstallTime
import app.simple.inure.apk.utils.PackageUtils.getApplicationLastUpdateTime
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.SDKHelper
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import net.lingala.zip4j.ZipFile
import java.io.File
import java.text.NumberFormat

class AppInformationViewModel(application: Application, private var packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val information: MutableLiveData<ArrayList<Pair<Int, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Spannable>>>().also {
            viewModelScope.launch(Dispatchers.IO) {
                loadInformation()
            }
        }
    }

    fun getInformation(): LiveData<ArrayList<Pair<Int, Spannable>>> {
        return information
    }

    private fun loadInformation() {
        kotlin.runCatching {
            if (packageInfo.applicationInfo.sourceDir.endsWith(".apkm")
                || packageInfo.applicationInfo.sourceDir.endsWith(".apks")
                || packageInfo.applicationInfo.sourceDir.endsWith(".zip")) {

                val zipFile = ZipFile(packageInfo.applicationInfo.sourceDir)
                val file = applicationContext().getInstallerDir("temp")

                file.delete()
                zipFile.extractFile("base.apk", file.absolutePath)

                packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.PackageInfoFlags.of(PackageUtils.flags))!!
                } else {
                    @Suppress("DEPRECATION")
                    packageManager.getPackageArchiveInfo(file.absolutePath, PackageUtils.flags.toInt())!!
                }

                packageInfo.applicationInfo.sourceDir = file.absolutePath
                packageInfo.splitNames = zipFile.fileHeaders.map { it.fileName }.toTypedArray()
            }
        }.getOrElse {
            it.printStackTrace()
            postError(it)
            return@getOrElse
        }

        information.postValue(arrayListOf(
                getPackageName(),
                getVersion(),
                getVersionCode(),
                getInstallLocation(),
                getDataDir(),
                getApkPath(),
                getGlesVersion(),
                getArchitecture(),
                getNativeLibraries(),
                getNativeLibsDir(),
                getUID(),
                getInstallDate(),
                getUpdateDate(),
                getMinSDK(),
                getTargetSDK(),
                getMethodCount(),
                getApex(),
                getApplicationType(),
                getInstaller(),
                getRequestedPermissions(),
                getFeatures(),
                getSplitNames()
        ))
    }

    private fun getPackageName(): Pair<Int, Spannable> {
        return Pair(R.string.package_name,
                    packageInfo.packageName.applySecondaryTextColor())
    }

    private fun getApkPath(): Pair<Int, Spannable> {
        val apkPath = kotlin.runCatching {
            packageInfo.applicationInfo.sourceDir
        }.getOrElse {
            null
        }

        return Pair(R.string.apk_base_package, apkPath?.applySecondaryTextColor() ?: getString(R.string.not_available).applySecondaryTextColor())
    }

    private fun getDataDir(): Pair<Int, Spannable> {
        return Pair(R.string.data,
                    packageInfo.applicationInfo.dataDir.applySecondaryTextColor())
    }

    private fun getVersion(): Pair<Int, Spannable> {
        return if (packageManager.isPackageInstalled(packageInfo.packageName)) {
            Pair(R.string.version,
                 PackageUtils.getApplicationVersion(context, packageInfo).applySecondaryTextColor())
        } else {
            val versionCode = ApkFile(packageInfo.applicationInfo.sourceDir).use {
                it.apkMeta.versionName
            }

            Pair(R.string.version, versionCode.toString().applySecondaryTextColor())
        }
    }

    private fun getVersionCode(): Pair<Int, Spannable> {
        return if (packageManager.isPackageInstalled(packageInfo.packageName)) {
            Pair(R.string.version_code,
                 PackageUtils.getApplicationVersion(context, packageInfo).applySecondaryTextColor())
        } else {
            val versionCode = ApkFile(packageInfo.applicationInfo.sourceDir).use {
                it.apkMeta.versionCode
            }

            Pair(R.string.version_code, versionCode.toString().applySecondaryTextColor())
        }
    }

    private fun getInstallLocation(): Pair<Int, Spannable> {
        val installLocation = kotlin.runCatching {
            when (packageInfo.installLocation) {
                PackageInfo.INSTALL_LOCATION_AUTO -> getString(R.string.auto)
                PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY -> getString(R.string.internal)
                PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL -> getString(R.string.prefer_external)
                else -> {
                    if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
                        getString(R.string.system)
                    } else {
                        getString(R.string.not_available)
                    }
                }
            }
        }.getOrElse {
            if (packageManager.isPackageInstalled(packageInfo.packageName)) {
                getString(R.string.app_not_installed, packageInfo.packageName)
            } else {
                getString(R.string.not_available)
            }
        }

        return Pair(R.string.install_location,
                    installLocation.applySecondaryTextColor())
    }

    private fun getGlesVersion(): Pair<Int, Spannable> {
        val glesVersion = kotlin.runCatching {
            File(packageInfo.applicationInfo.sourceDir).getGlEsVersion().ifEmpty { getString(R.string.not_available) }
        }.getOrElse {
            getString(R.string.not_available)
        }

        return Pair(R.string.gles_version,
                    glesVersion.applySecondaryTextColor())
    }

    private fun getArchitecture(): Pair<Int, Spannable> {
        return Pair(R.string.architecture,
                    packageInfo.applicationInfo.sourceDir.toFile().getApkArchitecture(context).toString().applyAccentColor())
    }

    private fun getNativeLibraries(): Pair<Int, Spannable> {
        return Pair(R.string.native_libraries,
                    packageInfo.applicationInfo.sourceDir.toFile().getNativeLibraries(context).toString().applySecondaryTextColor())
    }

    private fun getNativeLibsDir(): Pair<Int, Spannable> {
        val nativeLibsDir = kotlin.runCatching {
            packageInfo.applicationInfo.nativeLibraryDir
        }.getOrElse {
            null
        }

        return Pair(R.string.native_libraries_dir,
                    nativeLibsDir?.applySecondaryTextColor() ?: getString(R.string.not_available).applySecondaryTextColor())
    }

    private fun getUID(): Pair<Int, Spannable> {
        return Pair(R.string.uid,
                    packageInfo.applicationInfo.uid.toString().applySecondaryTextColor())
    }

    private fun getInstallDate(): Pair<Int, Spannable> {
        return Pair(R.string.install_date,
                    packageInfo.getApplicationInstallTime(context, FormattingPreferences.getDateFormat()).applyAccentColor())
    }

    private fun getUpdateDate(): Pair<Int, Spannable> {
        return Pair(R.string.update_date,
                    packageInfo.getApplicationLastUpdateTime(context, FormattingPreferences.getDateFormat()).applyAccentColor())
    }

    private fun getMinSDK(): Pair<Int, Spannable> {
        val minSdk = kotlin.runCatching {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                "${packageInfo.applicationInfo.minSdkVersion}, ${SDKHelper.getSdkTitle(packageInfo.applicationInfo.minSdkVersion)}"
            } else {
                when (val apkMeta: Any = packageInfo.applicationInfo.getApkMeta()) {
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
            "${packageInfo.applicationInfo.targetSdkVersion}, ${SDKHelper.getSdkTitle(packageInfo.applicationInfo.targetSdkVersion)}"
        }.getOrElse {
            it.message!!
        }

        return Pair(R.string.target_sdk,
                    targetSdk.applyAccentColor())
    }

    private fun getMethodCount(): Pair<Int, Spannable> {
        val method = kotlin.runCatching {
            val p0 = packageInfo.applicationInfo.sourceDir.toFile().getDexData()!!
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

    private fun getApex(): Pair<Int, Spannable> {
        val apex = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (packageInfo.isApex)
                getString(R.string.yes) else getString(R.string.no)
        } else {
            getString(R.string.not_available)
        }

        return Pair(R.string.apex,
                    apex.applySecondaryTextColor())
    }

    private fun getApplicationType(): Pair<Int, Spannable> {
        val applicationType = if (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
            getString(R.string.system)
        } else {
            getString(R.string.user)
        }

        return Pair(R.string.application_type,
                    applicationType.applySecondaryTextColor())
    }

    private fun getInstaller(): Pair<Int, Spannable> {
        @Suppress("deprecation")
        val name = kotlin.runCatching {
            val p0 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                application.packageManager.getInstallSourceInfo(packageInfo.packageName).installingPackageName
            } else {
                application.packageManager.getInstallerPackageName(packageInfo.packageName)
            }

            PackageUtils.getApplicationName(context, p0!!)
        }.getOrElse {
            if (packageManager.isPackageInstalled(packageInfo.packageName)) {
                getString(R.string.unknown)
            } else {
                getString(R.string.app_not_installed, packageInfo.packageName)
            }
        }

        return Pair(R.string.installer,
                    name!!.applySecondaryTextColor())
    }

    private fun getRequestedPermissions(): Pair<Int, Spannable> {
        val permissions = StringBuilder()

        try {
            packageInfo.requestedPermissions.sort()

            for (permission in packageInfo.requestedPermissions) {
                if (permissions.isEmpty()) {
                    permissions.append(permission)
                } else {
                    permissions.append("\n")
                    permissions.append(permission)
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            permissions.append(R.string.no_permissions_required)
        } catch (e: PackageManager.NameNotFoundException) {
            permissions.append(R.string.app_not_installed, packageInfo.packageName)
        }

        return Pair(R.string.permissions,
                    permissions.toString().applySecondaryTextColor())
    }

    private fun getSplitNames(): Pair<Int, Spannable> {
        val names = StringBuilder()

        try {
            for (name in packageInfo.splitNames) {
                if (names.isEmpty()) {
                    names.append(name)
                } else {
                    names.append("\n")
                    names.append(name)
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            names.append(getString(R.string.not_available))
        }

        if (names.isEmpty())
            names.append(getString(R.string.not_available))

        return Pair(R.string.split_packages,
                    names.toString().applySecondaryTextColor())
    }

    private fun getFeatures(): Pair<Int, Spannable> {
        val features = StringBuilder()

        try {
            for (feature in packageInfo.reqFeatures) {
                if (features.isEmpty()) {
                    if (feature.name.isNullOrEmpty()) {
                        features.append(MetaUtils.getOpenGL(feature.reqGlEsVersion))
                    } else {
                        features.append(feature.name)
                    }
                } else {
                    features.append("\n")
                    if (feature.name.isNullOrEmpty()) {
                        features.append(MetaUtils.getOpenGL(feature.reqGlEsVersion))
                    } else {
                        features.append(feature.name)
                    }
                }
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
            features.append(getString(R.string.not_available))
        } catch (e: PackageManager.NameNotFoundException) {
            features.append(getString(R.string.app_not_installed), packageInfo.packageName)
        }

        return Pair(R.string.uses_feature,
                    features.toString().applySecondaryTextColor())
    }
}