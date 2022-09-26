package app.simple.inure.models

import android.content.pm.PackageInfo

class PackageStats {
    /**
     * Package information
     */
    var packageInfo: PackageInfo? = null

    /**
     * Total time the app is being used
     */
    var totalTimeUsed = 0L

    /**
     * Total time of the last usage
     */
    var lastUsageTime = 0L

    /**
     * Total mobile data
     */
    var mobileData: DataUsage? = null

    /**
     * Total wifi data
     */
    var wifiData: DataUsage? = null

    /**
     * Total launch count
     */
    var launchCount = 0

    /**
     * App usage data
     */
    var appUsage: ArrayList<AppUsageModel>? = null
}