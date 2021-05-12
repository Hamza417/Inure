package app.simple.inure.model

import android.content.pm.ApplicationInfo

class PackageStats {
    /**
     *
     */
    lateinit var applicationInfo: ApplicationInfo

    var numberOfTimeOpened = 0

    var totalTimeUsed = 0L

    var dataSent = 0L

    var dataReceived = 0L
}