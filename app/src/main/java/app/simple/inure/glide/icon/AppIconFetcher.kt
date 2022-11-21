package app.simple.inure.glide.icon

import android.content.Context
import android.content.pm.LauncherApps
import android.content.pm.PackageManager
import android.graphics.Bitmap
import app.simple.inure.R
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.BitmapHelper.toBitmap
import app.simple.inure.util.BitmapHelper.toGrayscale
import com.bumptech.glide.Priority
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.data.DataFetcher

class AppIconFetcher internal constructor(private val appIcon: AppIcon) : DataFetcher<Bitmap> {

    override fun loadData(priority: Priority, callback: DataFetcher.DataCallback<in Bitmap>) {
        try {
            kotlin.runCatching {
                // try getting the properly colored launcher icons
                val launcher = appIcon.context.getSystemService(Context.LAUNCHER_APPS_SERVICE) as LauncherApps
                val activityList = launcher.getActivityList(appIcon.packageName, android.os.Process.myUserHandle())[0]
                if (appIcon.enabled) {
                    callback.onDataReady(activityList.getBadgedIcon(0).toBitmap())
                } else {
                    callback.onDataReady(activityList.getBadgedIcon(0).toBitmap()?.toGrayscale())
                }
            }.onFailure {
                // Loading proper icon failed, try loading default icon
                if (appIcon.enabled) {
                    callback.onDataReady(
                            appIcon.context.packageManager
                                .getApplicationIcon(appIcon.packageName)
                                .toBitmap())
                } else {
                    callback.onDataReady(
                            appIcon.context.packageManager
                                .getApplicationIcon(appIcon.packageName)
                                .toBitmap()?.toGrayscale())
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            callback.onDataReady(R.drawable.ic_app_icon.toBitmap(appIcon.context, AppearancePreferences.getIconSize()))
        } catch (e: NullPointerException) {
            callback.onDataReady(R.drawable.ic_app_icon.toBitmap(appIcon.context, AppearancePreferences.getIconSize()))
        } catch (e: Exception) {
            callback.onLoadFailed(e)
        }
    }

    override fun cleanup() {
        /* no-op */
    }

    override fun cancel() {
        /* no-op */
    }

    override fun getDataClass(): Class<Bitmap> {
        return Bitmap::class.java
    }

    override fun getDataSource(): DataSource {
        return DataSource.LOCAL
    }
}
