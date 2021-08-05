package app.simple.inure.model

import android.content.pm.PackageInfo

class PackageStats {
    /**
     * Package information
     */
    var packageInfo: PackageInfo? = null

    /**
     * Number of times app is opened
     */
    var numberOfTimeOpened = 0

    /**
     * Total time the app is being used
     */
    var totalTimeUsed = 0L

    /**
     * Total data uploaded
     */
    var dataSent = 0L

    /**
     * Total data received
     */
    var dataReceived = 0L

    /**
     * Total data sent via wifi
     */
    var dataSentWifi = 0L

    /**
     * Total data received via wifi
     */
    var dataReceivedWifi = 0L

    /**
     * Total launch count
     */
    var launchCount = 0
}