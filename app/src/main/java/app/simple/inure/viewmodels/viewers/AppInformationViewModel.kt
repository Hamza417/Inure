package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager.NameNotFoundException
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
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getApplicationInstallTime
import app.simple.inure.apk.utils.PackageUtils.getApplicationLastUpdateTime
import app.simple.inure.apk.utils.PackageUtils.getPackageArchiveInfo
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.getPackageSize
import app.simple.inure.apk.utils.PackageUtils.getXposedDescription
import app.simple.inure.apk.utils.PackageUtils.isBackupAllowed
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.isXposedModule
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.exceptions.DexClassesNotFoundException
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.preferences.FormattingPreferences
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.SDKUtils
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import app.simple.inure.util.StringUtils.endsWithAny
import app.simple.inure.util.TrackerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
            if (packageInfo.safeApplicationInfo.sourceDir.endsWithAny(".zip", ".xapk", ".apks", ".apkm")) {
                val zipFile = ZipFile(packageInfo.safeApplicationInfo.sourceDir)
                val file = applicationContext().getInstallerDir("temp")

                file.deleteRecursively()
                file.mkdirs()
                zipFile.extractAll(file.path)

                for (apkFile in file.listFiles()!!) {
                    if (apkFile.absolutePath.endsWith(".apk", ignoreCase = true)) {
                        packageInfo = packageManager.getPackageArchiveInfo(apkFile.absolutePath)!!
                        packageInfo.safeApplicationInfo.sourceDir = apkFile.absolutePath
                        packageInfo.safeApplicationInfo.publicSourceDir = apkFile.absolutePath
                        packageInfo.splitNames = zipFile.fileHeaders.map { it.fileName }.toTypedArray()
                        break
                    }
                }

                packageInfo.safeApplicationInfo.sourceDir = file.absolutePath
                packageInfo.splitNames = zipFile.fileHeaders.map { it.fileName }.toTypedArray()

                isPackageInstalled = packageManager.isPackageInstalled(packageInfo.packageName)
            } else {
                try {
                    packageInfo = packageManager.getPackageInfo(packageInfo.packageName) ?: throw NullPointerException()
                    isPackageInstalled = true
                } catch (e: java.lang.NullPointerException) {
                    /**
                     * Activity should have already sent a prepared
                     * [PackageInfo] object to this ViewModel for
                     * processing. If it is null, then the there's
                     * some error on the activity side.
                     */
                    isPackageInstalled = false
                } catch (e: NameNotFoundException) {
                    isPackageInstalled = false
                }
            }
        }.getOrElse {
            it.printStackTrace()
            postError(it)
            return@getOrElse
        }

        val informationList = arrayListOf<Pair<Int, Spannable>>()

        kotlin.runCatching {
            informationList.add(getPackageName())
            informationList.add(getVersion())
            informationList.add(getVersionCode())
            informationList.add(getInstallLocation())
            informationList.add(getState())
            informationList.add(getDataDir())
            informationList.add(getCacheSize())
            informationList.add(getApkPath())
            informationList.add(getSplitNames())
            informationList.add(getBackup())
            informationList.add(getTrackers())
            informationList.add(getGlesVersion())
            informationList.add(getArchitecture())
            informationList.add(getNativeLibraries())
            informationList.add(getNativeLibsDir())
            informationList.add(getUID())
            informationList.add(getInstallDate())
            informationList.add(getUpdateDate())
            informationList.add(getMinSDK())
            informationList.add(getTargetSDK())
            informationList.add(getFOSS())
            if (FOSSParser.isPackageFOSS(packageInfo)) {
                informationList.add(getFOSSLicense())
            }
            informationList.add(getXposedModule())
            if (packageInfo.safeApplicationInfo.isXposedModule()) {
                informationList.add(getXposedDescription())
            }
            informationList.add(getMethodCount())
            informationList.add(getApex())
            informationList.add(getApplicationType())
            informationList.add(getInstaller())
            informationList.add(getRequestedPermissions())
            informationList.add(getFeatures())

            information.postValue(informationList)
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
                packageInfo.safeApplicationInfo.sourceDir + " | " + packageInfo.safeApplicationInfo.sourceDir.toSize()
            } else {
                null
            }
        }.getOrElse {
            null
        }

        return Pair(R.string.apk_base_package, apkPath?.applySecondaryTextColor()
            ?: getString(R.string.not_available).applySecondaryTextColor())
    }

    private fun getState(): Pair<Int, Spannable> {
        val string = if (isPackageInstalled) {
            buildString {
                append(getString(R.string.installed))
                append(" | ")
                if (packageInfo.safeApplicationInfo.enabled) {
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
            // val s = packageInfo.safeApplicationInfo.dataDir + " | " + packageInfo.safeApplicationInfo.dataDir.getDirectorySize()
            return Pair(R.string.data, packageInfo.safeApplicationInfo.dataDir.applySecondaryTextColor())
        }.getOrElse {
            return Pair(R.string.data, getString(R.string.not_available).applySecondaryTextColor())
        }
    }

    private fun getCacheSize(): Pair<Int, Spannable> {
        kotlin.runCatching {
            val packageSizes = packageInfo.getPackageSize(applicationContext())
            packageSizes.cacheSize.let {
                val s = it.toSize()
                return Pair(R.string.cache, s.applySecondaryTextColor())
            }
        }.getOrElse {
            return Pair(R.string.cache, getString(R.string.not_available).applySecondaryTextColor())
        }
    }

    private fun getVersion(): Pair<Int, Spannable> {
        return Pair(R.string.version,
                    PackageUtils.getApplicationVersion(context, packageInfo).applySecondaryTextColor())
    }

    private fun getVersionCode(): Pair<Int, Spannable> {
        return Pair(R.string.version_code,
                    PackageUtils.getApplicationVersionCode(context, packageInfo).applySecondaryTextColor())
    }

    private fun getInstallLocation(): Pair<Int, Spannable> {
        val installLocation = kotlin.runCatching {
            when (packageInfo.installLocation) {
                PackageInfo.INSTALL_LOCATION_AUTO -> getString(R.string.auto)
                PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY -> getString(R.string.internal)
                PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL -> getString(R.string.prefer_external)
                else -> {
                    if (packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
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
            File(packageInfo.safeApplicationInfo.sourceDir)
                .getGlEsVersion().ifEmpty { getString(R.string.not_available) }
        }.getOrElse {
            getString(R.string.not_available)
        }

        return Pair(R.string.gles_version,
                    glesVersion.applySecondaryTextColor())
    }

    private fun getArchitecture(): Pair<Int, Spannable> {
        return Pair(R.string.architecture,
                    packageInfo.safeApplicationInfo.sourceDir.toFile()
                        .getApkArchitecture(context).applyAccentColor())
    }

    private fun getNativeLibraries(): Pair<Int, Spannable> {
        kotlin.runCatching {
            return Pair(R.string.native_libraries,
                        packageInfo.getNativeLibraries(context).applySecondaryTextColor())
        }.getOrElse {
            return Pair(R.string.native_libraries,
                        getString(R.string.not_available).applySecondaryTextColor())
        }
    }

    private fun getNativeLibsDir(): Pair<Int, Spannable> {
        val nativeLibsDir = kotlin.runCatching {
            packageInfo.safeApplicationInfo.nativeLibraryDir
        }.getOrElse {
            null
        }

        return Pair(R.string.native_libraries_dir,
                    nativeLibsDir?.applySecondaryTextColor()
                        ?: getString(R.string.not_available).applySecondaryTextColor())
    }

    private fun getUID(): Pair<Int, Spannable> {
        return Pair(R.string.uid,
                    packageInfo.safeApplicationInfo.uid.toString().applySecondaryTextColor())
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
                "${packageInfo.safeApplicationInfo.minSdkVersion}, ${SDKUtils.getSdkTitle(packageInfo.safeApplicationInfo.minSdkVersion)}"
            } else {
                with(packageInfo.safeApplicationInfo.getApkMeta()) {
                    "${minSdkVersion}, ${SDKUtils.getSdkTitle(minSdkVersion)}"
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
            "${packageInfo.safeApplicationInfo.targetSdkVersion}, " +
                    SDKUtils.getSdkTitle(packageInfo.safeApplicationInfo.targetSdkVersion)
        }.getOrElse {
            it.message!!
        }

        return Pair(R.string.target_sdk,
                    targetSdk.applyAccentColor())
    }

    private fun getFOSS(): Pair<Int, Spannable> {
        FOSSParser.init(applicationContext())
        val isFOSS = FOSSParser.isPackageFOSS(packageInfo)
        return Pair(R.string.foss,
                    (if (isFOSS) getString(R.string.yes) else getString(R.string.no))
                        .applySecondaryTextColor())
    }

    private fun getFOSSLicense(): Pair<Int, Spannable> {
        FOSSParser.init(applicationContext())
        val licenses = FOSSParser.getPackageLicense(packageInfo)
        return Pair(R.string.open_source_licenses,
                    licenses.toString().applySecondaryTextColor())
    }

    private fun getXposedModule(): Pair<Int, Spannable> {
        val string = buildString {
            if (packageInfo.safeApplicationInfo.isXposedModule()) {
                append(getString(R.string.yes))
            } else {
                append(getString(R.string.no))
            }
        }

        return Pair(R.string.xposed_module, string.applySecondaryTextColor())
    }

    private fun getXposedDescription(): Pair<Int, Spannable> {
        return Pair(R.string.description,
                    packageInfo.safeApplicationInfo.getXposedDescription().applySecondaryTextColor())
    }

    private fun getMethodCount(): Pair<Int, Spannable> {
        var count = 0
        val method = kotlin.runCatching {
            val dexClasses = try {
                packageInfo.safeApplicationInfo.sourceDir.toFile().getDexData()
            } catch (e: DexClassesNotFoundException) {
                packageInfo.safeApplicationInfo.publicSourceDir.toFile().getDexData()
            }

            for (clazz in dexClasses) {
                count += clazz.javaClass.methods.size
            }

            val dexClassesCount = java.util.zip.ZipFile(packageInfo.safeApplicationInfo.sourceDir).use {
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
        val applicationType = if (packageInfo.safeApplicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0) {
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
                packageManager.getInstallSourceInfo(packageInfo.packageName).installingPackageName
            } else {
                packageManager.getInstallerPackageName(packageInfo.packageName)
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
            packageInfo.requestedPermissions!!.sort()

            for (permission in packageInfo.requestedPermissions!!) {
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
        } catch (e: NameNotFoundException) {
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

    private fun getBackup(): Pair<Int, Spannable> {
        val isBackupAllowed = packageInfo.isBackupAllowed()
        val spannable = if (isBackupAllowed) {
            getString(R.string.allowed)
        } else {
            getString(R.string.not_allowed)
        }
        return Pair(R.string.backup, spannable.applySecondaryTextColor())
    }

    private fun getTrackers(): Pair<Int, Spannable> {
        val trackers = TrackerUtils.getTrackersData()
        var count = 0
        val list: MutableList<String> = mutableListOf()

        if (packageInfo.activities != null) {
            for (activity in packageInfo.activities!!) {
                for (tracker in trackers) {
                    tracker.codeSignature.split("|").forEach {
                        if (activity.name.lowercase().contains(it.lowercase())) {
                            count++
                            list.add(tracker.name)
                            return@forEach
                        }
                    }
                }
            }
        }

        if (packageInfo.services != null) {
            for (service in packageInfo.services!!) {
                for (tracker in trackers) {
                    tracker.codeSignature.split("|").forEach {
                        if (service.name.lowercase().contains(it.lowercase())) {
                            count++
                            list.add(tracker.name)
                            return@forEach
                        }
                    }
                }
            }
        }

        if (packageInfo.receivers != null) {
            for (receiver in packageInfo.receivers!!) {
                for (tracker in trackers) {
                    tracker.codeSignature.split("|").forEach {
                        if (receiver.name.lowercase().contains(it.lowercase())) {
                            count++
                            list.add(tracker.name)
                            return@forEach
                        }
                    }
                }
            }
        }

        if (packageInfo.providers != null) {
            for (provider in packageInfo.providers!!) {
                for (tracker in trackers) {
                    tracker.codeSignature.split("|").forEach {
                        if (provider.name.lowercase().contains(it.lowercase())) {
                            count++
                            list.add(tracker.name)
                            return@forEach
                        }
                    }
                }
            }
        }

        buildString {
            for (tracker in list.distinct()) {
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
            for (feature in packageInfo.reqFeatures!!) {
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
        } catch (e: NameNotFoundException) {
            features.append(getString(R.string.app_not_installed), packageInfo.packageName)
        }

        return Pair(R.string.uses_feature,
                    features.toString().applySecondaryTextColor())
    }
}
