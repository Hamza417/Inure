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
import app.simple.inure.apk.utils.PackageUtils.getPackageArchiveInfo
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.getPackageSize
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.exceptions.DexClassesNotFoundException
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.SDKHelper
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import app.simple.inure.util.StringUtils.endsWithAny
import app.simple.inure.util.TrackerUtils.getTrackerSignatures
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import net.dongliu.apk.parser.bean.ApkMeta
import net.lingala.zip4j.ZipFile
import java.io.File
import java.text.NumberFormat

class AppInformationViewModel(application: Application, private var packageInfo: PackageInfo) : WrappedViewModel(application) {

    private var isPackageInstalled = false

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
            if (packageInfo.applicationInfo.sourceDir.endsWithAny(".zip", ".xapk", ".apks", ".apkm")) {
                val zipFile = ZipFile(packageInfo.applicationInfo.sourceDir)
                val file = applicationContext().getInstallerDir("temp")

                file.deleteRecursively()
                file.mkdirs()
                zipFile.extractAll(file.path)

                for (apkFile in file.listFiles()!!) {
                    if (apkFile.absolutePath.endsWith(".apk", ignoreCase = true)) {
                        packageInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath)!!
                        packageInfo.applicationInfo.sourceDir = apkFile.absolutePath
                        packageInfo.applicationInfo.publicSourceDir = apkFile.absolutePath
                        packageInfo.splitNames = zipFile.fileHeaders.map { it.fileName }.toTypedArray()
                        break
                    }
                }

                packageInfo.applicationInfo.sourceDir = file.absolutePath
                packageInfo.splitNames = zipFile.fileHeaders.map { it.fileName }.toTypedArray()

                isPackageInstalled = packageManager.isPackageInstalled(packageInfo.packageName)
            } else {
                try {
                    packageInfo = packageManager.getPackageInfo(packageInfo.packageName)!!
                    isPackageInstalled = true
                } catch (e: java.lang.NullPointerException) {
                    isPackageInstalled = false
                    packageInfo = packageManager.getPackageArchiveInfo(packageInfo.applicationInfo.sourceDir)!!
                }
            }
        }.getOrElse {
            it.printStackTrace()
            postError(it)
            return@getOrElse
        }

        kotlin.runCatching {
            information.postValue(arrayListOf(
                    getPackageName(),
                    getVersion(),
                    getVersionCode(),
                    getInstallLocation(),
                    getState(),
                    getDataDir(),
                    getCacheSize(),
                    getApkPath(),
                    getSplitNames(),
                    getTrackers(),
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
            ))
        }.onFailure {
            it.printStackTrace()
            postWarning(it.toString())
        }
    }

    private fun getPackageName(): Pair<Int, Spannable> {
        return Pair(R.string.package_name,
                    packageInfo.packageName.applySecondaryTextColor())
    }

    private fun getApkPath(): Pair<Int, Spannable> {
        val apkPath = kotlin.runCatching {
            if (isPackageInstalled) {
                packageInfo.applicationInfo.sourceDir + " | " + packageInfo.applicationInfo.sourceDir.toSize()
            } else {
                null
            }
        }.getOrElse {
            null
        }

        return Pair(R.string.apk_base_package, apkPath?.applySecondaryTextColor() ?: getString(R.string.not_available).applySecondaryTextColor())
    }

    private fun getState(): Pair<Int, Spannable> {
        val string = if (isPackageInstalled) {
            buildString {
                append(getString(R.string.installed))
                append(" | ")
                if (packageInfo.applicationInfo.enabled) {
                    append(getString(R.string.enabled))
                } else {
                    append(getString(R.string.disabled))
                }
            }
        } else {
            buildString {
                append(getString(R.string.app_not_installed, packageInfo.packageName))
            }
        }

        return Pair(R.string.state, string.applyAccentColor())
    }

    private fun getDataDir(): Pair<Int, Spannable> {
        kotlin.runCatching {
            // val s = packageInfo.applicationInfo.dataDir + " | " + packageInfo.applicationInfo.dataDir.getDirectorySize()
            return Pair(R.string.data, packageInfo.applicationInfo.dataDir.applySecondaryTextColor())
        }.getOrElse {
            return Pair(R.string.data, getString(R.string.not_available).applySecondaryTextColor())
        }
    }

    private fun getCacheSize(): Pair<Int, Spannable> {
        kotlin.runCatching {
            val packageSizes = packageInfo.getPackageSize(application)
            packageSizes.cacheSize.let {
                val s = it.toSize()
                return Pair(R.string.cache, s.applySecondaryTextColor())
            }
        }.getOrElse {
            return Pair(R.string.cache, getString(R.string.not_available).applySecondaryTextColor())
        }
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
                 PackageUtils.getApplicationVersionCode(context, packageInfo).applySecondaryTextColor())
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
        var count = 0
        val method = kotlin.runCatching {
            val dexClasses = try {
                packageInfo.applicationInfo.sourceDir.toFile().getDexData()
            } catch (e: DexClassesNotFoundException) {
                packageInfo.applicationInfo.publicSourceDir.toFile().getDexData()
            }

            for (clazz in dexClasses) {
                count += clazz.javaClass.methods.size
            }

            val dexClassesCount = java.util.zip.ZipFile(packageInfo.applicationInfo.sourceDir).use {
                var dexCount = 0

                with(it.entries()) {
                    while (hasMoreElements()) {
                        if (nextElement().name.endsWith(".dex")) {
                            dexCount += 1
                        }
                    }
                }

                dexCount
            }

            if (dexClassesCount > 1) {
                String.format(getString(R.string.multi_dex), NumberFormat.getNumberInstance().format(count))
            } else {
                String.format(getString(R.string.single_dex), NumberFormat.getNumberInstance().format(count))
            }
        }.getOrElse {
            it.message!!
        }

        return Pair(R.string.method_count, method.applySecondaryTextColor())
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
            permissions.append(getString(R.string.no_permissions_required))
        } catch (e: PackageManager.NameNotFoundException) {
            permissions.append(getString(R.string.app_not_installed, packageInfo.packageName))
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

    private fun getTrackers(): Pair<Int, Spannable> {
        val trackers = applicationContext().getTrackerSignatures()
        var count = 0
        val list: MutableList<String> = mutableListOf()

        if (packageInfo.activities != null) {
            for (activity in packageInfo.activities) {
                for (tracker in trackers) {
                    if (activity.name.lowercase().contains(tracker.lowercase())) {
                        count++
                        list.add(activity.name)
                        break
                    }
                }
            }
        }

        if (packageInfo.services != null) {
            for (service in packageInfo.services) {
                for (tracker in trackers) {
                    if (service.name.lowercase().contains(tracker.lowercase())) {
                        count++
                        list.add(service.name)
                        break
                    }
                }
            }
        }

        if (packageInfo.receivers != null) {
            for (receiver in packageInfo.receivers) {
                for (tracker in trackers) {
                    if (receiver.name.lowercase().contains(tracker.lowercase())) {
                        count++
                        list.add(receiver.name)
                        break
                    }
                }
            }
        }

        buildString {
            for (tracker in list) {
                if (this.isEmpty()) {
                    append(getString(R.string.trackers_count, count))
                    append("\n\n")
                    append(tracker)
                } else {
                    append("\n")
                    append(tracker)
                }
            }

            if (this.isEmpty()) {
                append(getString(R.string.no_trackers_found))
            }

            return Pair(R.string.trackers,
                        this.toString().applySecondaryTextColor())
        }
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