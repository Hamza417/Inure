package app.simple.inure.activities.association

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageData.getInstallerDir
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.activities.BaseActivity
import app.simple.inure.ui.viewers.Information
import app.simple.inure.util.FileUtils
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.ParcelUtils.parcelable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class InformationActivity : BaseActivity() {
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
                        val sourceFile = applicationContext.getInstallerDir(name!!)
                        val packageInfo: PackageInfo

                        contentResolver.openInputStream(uri)!!.use {
                            FileUtils.copyStreamToFile(it, sourceFile)
                        }

                        if (sourceFile.absolutePath.endsWith(".apk")) {
                            packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                packageManager.getPackageArchiveInfo(sourceFile.absolutePath, PackageManager.PackageInfoFlags.of(PackageUtils.flags))!!
                            } else {
                                @Suppress("DEPRECATION")
                                packageManager.getPackageArchiveInfo(sourceFile.absolutePath, PackageUtils.flags.toInt())!!
                            }

                            packageInfo.applicationInfo.sourceDir = sourceFile.absolutePath
                        } else {
                            packageInfo = PackageInfo() // empty package info
                            packageInfo.applicationInfo = ApplicationInfo() // empty application info
                            packageInfo.applicationInfo.sourceDir = sourceFile.absolutePath
                        }

                        withContext(Dispatchers.Main) {
                            hideLoader()

                            supportFragmentManager.beginTransaction()
                                .setReorderingAllowed(true)
                                .replace(R.id.app_container, Information.newInstance(packageInfo), Information.TAG)
                                .commit()
                        }
                    }.getOrElse {
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
