package app.simple.inure.glide.apkIcon

import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import app.simple.inure.apk.utils.PackageData.getCachedDir
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.glide.util.GlideUtils.getGeneratedAppIconBitmap
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.BitmapHelper.toBitmap
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher
import net.dongliu.apk.parser.ApkFile
import net.lingala.zip4j.ZipFile
import java.io.ByteArrayInputStream
import java.io.File

class ApkIconFetcher internal constructor(private val apkIcon: ApkIcon) : DataFetcher<Bitmap> {

    private var zipFile: ZipFile? = null
    private var apkFile: File? = null

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        kotlin.runCatching {
            kotlin.runCatching {
                if (apkIcon.file.absolutePath.endsWith(".apk")) {
                    val p0 = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        apkIcon.context.packageManager
                            .getPackageArchiveInfo(apkIcon.file.path,
                                                   PackageManager.PackageInfoFlags.of(PackageManager.GET_META_DATA.toLong()))
                    } else {
                        @Suppress("DEPRECATION")
                        apkIcon.context.packageManager
                            .getPackageArchiveInfo(apkIcon.file.path,
                                                   PackageManager.GET_META_DATA)
                    }
                    p0!!.safeApplicationInfo.sourceDir = apkIcon.file.path
                    p0.safeApplicationInfo.publicSourceDir = apkIcon.file.path
                    val b = apkIcon.context.packageManager.getApplicationIcon(p0.safeApplicationInfo)
                    callback.onDataReady(b.toBitmap())
                } else {
                    if (ApkBrowserPreferences.isLoadSplitIcon()) {
                        zipFile = ZipFile(apkIcon.file.absoluteFile)
                        apkFile = apkIcon.context.getCachedDir(apkIcon.file.name.substringBeforeLast("."), "split_icons")

                        zipFile?.use {
                            it.extractFile("base.apk", apkFile?.absolutePath)
                        }

                        apkFile = File(apkFile?.absolutePath + "/base.apk")

                        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            apkIcon.context.packageManager.getPackageArchiveInfo(apkFile?.absolutePath!!, PackageManager.PackageInfoFlags.of(PackageUtils.flags))!!
                        } else {
                            @Suppress("DEPRECATION")
                            apkIcon.context.packageManager.getPackageArchiveInfo(apkFile?.absolutePath!!, PackageUtils.flags.toInt())!!
                        }

                        packageInfo.safeApplicationInfo.sourceDir = apkFile?.absolutePath
                        packageInfo.safeApplicationInfo.publicSourceDir = apkFile?.absolutePath
                        val b = apkIcon.context.packageManager.getApplicationIcon(packageInfo.safeApplicationInfo)
                        callback.onDataReady(b.toBitmap())
                    } else {
                        throw Exception("Split icon loading is disabled")
                    }
                }
            }.onFailure {
                val p0 = ApkFile(apkIcon.file).use {
                    it.allIcons
                }
                callback.onDataReady(BitmapFactory.decodeStream(ByteArrayInputStream(p0.last().data)))
            }
        }.getOrElse {
            it.printStackTrace()
            callback.onDataReady(apkIcon.context.getGeneratedAppIconBitmap())
        }
    }

    override fun cleanup() {
        /**
         * Clean it like you mean it
         */
        apkFile?.parent?.let { File(it).deleteRecursively() }
    }

    override fun cancel() {
        /* no-op */
        zipFile?.close()
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}
