package app.simple.inure.activities.association

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.apk.utils.PackageData.getOtherCacheDir
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getPackageArchiveInfo
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.panels.AppInfo
import app.simple.inure.util.FileUtils
import app.simple.inure.util.FileUtils.toFile
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ParcelUtils.parcelable
import com.anggrayudi.storage.file.baseName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.lingala.zip4j.ZipFile

class AppInformationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        kotlin.runCatching {
            if (savedInstanceState.isNull()) {
                showLoader()

                lifecycleScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        val uri = if (intent.action == Intent.ACTION_SEND) {
                            intent.parcelable(Intent.EXTRA_STREAM)!!
                        } else {
                            intent.data!!
                        }

                        val name = DocumentFile.fromSingleUri(applicationContext, uri)!!.name
                        val sourceFile = applicationContext.getOtherCacheDir(name!!)
                        var packageInfo: PackageInfo? = null

                        contentResolver.openInputStream(uri)!!.use {
                            FileUtils.copyStreamToFile(it, sourceFile)
                        }

                        when {
                            sourceFile.absolutePath.lowercase().endsWith(".apk") -> { // Single APK
                                packageInfo = packageManager.getPackageArchiveInfo(sourceFile.absolutePath)!!

                                packageInfo.safeApplicationInfo.publicSourceDir = sourceFile.absolutePath
                                packageInfo.safeApplicationInfo.sourceDir = sourceFile.absolutePath
                            }
                            sourceFile.absolutePath.endsWith(".apks") ||
                                    sourceFile.absolutePath.endsWith(".xapk") ||
                                    sourceFile.absolutePath.endsWith(".zip") ||
                                    sourceFile.absolutePath.endsWith(".apkm") -> { // Split APK
                                uri.let { it ->
                                    val documentFile = DocumentFile.fromSingleUri(applicationContext, it)!!
                                    val copiedFile = applicationContext.getInstallerDir(documentFile.baseName + ".zip")

                                    if (!copiedFile.exists()) {
                                        contentResolver.openInputStream(it).use {
                                            FileUtils.copyStreamToFile(it!!, copiedFile)
                                        }
                                    }

                                    ZipFile(copiedFile.path).extractAll(copiedFile.path.substringBeforeLast("."))

                                    for (file in copiedFile.path.substringBeforeLast(".").toFile().listFiles()!!) {
                                        packageInfo = packageManager.getPackageArchiveInfo(file.absolutePath.toFile()) ?: continue
                                        packageInfo!!.safeApplicationInfo.sourceDir = file.absolutePath
                                        packageInfo!!.safeApplicationInfo.publicSourceDir = file.absolutePath
                                        break
                                    }
                                }
                            }
                            else -> {
                                packageInfo = PackageInfo() // empty package info
                                packageInfo!!.safeApplicationInfo = ApplicationInfo() // empty application info
                                packageInfo!!.safeApplicationInfo.sourceDir = sourceFile.absolutePath
                            }
                        }

                        packageInfo!!.safeApplicationInfo.name = PackageUtils.getApplicationName(baseContext, packageInfo!!.safeApplicationInfo)
                        packageInfo!!.versionName = PackageUtils.getApplicationVersion(baseContext, packageInfo!!)

                        withContext(Dispatchers.Main) {
                            hideLoader()

                            supportFragmentManager.beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.app_container, AppInfo.newInstance(packageInfo!!), "app_info")
                                .commit()
                        }
                    }.getOrElse {
                        it.printStackTrace()
                        kotlin.runCatching {
                            withContext(Dispatchers.Main) {
                                hideLoader()
                                showWarning(it.localizedMessage ?: it.message ?: ("cannot parse file :" +
                                        " " + DocumentFile.fromSingleUri(applicationContext,
                                                                         intent.data ?: intent?.parcelable(Intent.EXTRA_STREAM)!!)!!.name))
                            }
                        }.getOrElse {
                            withContext(Dispatchers.Main) {
                                hideLoader()
                                showWarning(it.localizedMessage ?: it.message ?: "Unknown error occurred")
                            }
                        }
                    }
                }
            }
        }.getOrElse {
            showWarning(it.localizedMessage ?: it.message ?: "Unknown error occurred")
        }
    }
}
