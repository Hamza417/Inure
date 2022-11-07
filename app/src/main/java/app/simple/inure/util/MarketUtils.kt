package app.simple.inure.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import android.os.Build

object MarketUtils {

    /**
     * Opens app's page on Play Store
     *
     * @param context Context of the environment
     * @param packageName Package ID of the app
     */
    fun openAppOnPlayStore(context: Context, packageName: String) {

        val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        var marketFound = false

        /**
         * find all applications able to handle our rateIntent
         */
        val otherApps: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(rateIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(rateIntent, 0)
        }

        for (otherApp in otherApps) {
            /**
             * look for Google Play application
             */
            if (otherApp.activityInfo.applicationInfo.packageName == "com.android.vending") {
                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name)

                /**
                 * make sure it does NOT open in the stack of your activity
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                /**
                 * task re -parenting if needed
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                /**
                 * if the Google Play was already open in a search result
                 * this make sure it still go to the app page you requested
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                /**
                 * this make sure only the Google Play app is allowed to
                 * intercept the intent
                 */
                rateIntent.component = componentName
                context.startActivity(rateIntent)
                marketFound = true

                break
            }
        }

        /**
         * if GP not present on device, open web browser
         */
        if (!marketFound) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
            context.startActivity(webIntent)
        }
    }

    /**
     * Opens app's page on Amazon App Store
     *
     * @param context Context of the environment
     * @param packageName Package ID of the app
     */
    fun openAppOnAmazonStore(context: Context, packageName: String) {

        val rateIntent = Intent(Intent.ACTION_VIEW, Uri.parse("amzn://apps/android?p=$packageName"))
        var marketFound = false

        /**
         * find all applications able to handle our rateIntent
         */
        val otherApps: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.queryIntentActivities(rateIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.queryIntentActivities(rateIntent, 0)
        }

        for (otherApp in otherApps) {
            /**
             * look for Google Play application
             */
            if (otherApp.activityInfo.applicationInfo.packageName == "com.amazon.venezia") {
                val otherAppActivity = otherApp.activityInfo
                val componentName = ComponentName(otherAppActivity.applicationInfo.packageName, otherAppActivity.name)

                /**
                 * make sure it does NOT open in the stack of your activity
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                /**
                 * task re -parenting if needed
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)

                /**
                 * if the Google Play was already open in a search result
                 * this make sure it still go to the app page you requested
                 */
                rateIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                /**
                 * this make sure only the Google Play app is allowed to
                 * intercept the intent
                 */
                rateIntent.component = componentName
                context.startActivity(rateIntent)
                marketFound = true

                break
            }
        }

        /**
         * if GP not present on device, open web browser
         */
        if (!marketFound) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://www.amazon.com/gp/mas/dl/android?p=$packageName"))
            context.startActivity(webIntent)
        }
    }

    /**
     * Opens app's page on FDroid
     *
     * @param context Context of the environment
     * @param packageName Package ID of the app
     */
    fun openAppOnFdroid(context: Context, packageName: String) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.f-droid.org/en/packages/$packageName\\/"))
        context.startActivity(webIntent)
    }
}