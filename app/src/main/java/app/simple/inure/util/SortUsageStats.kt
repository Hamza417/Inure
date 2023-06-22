package app.simple.inure.util

import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.StatisticsPreferences
import java.util.*

object SortUsageStats {
    const val TIME_USED = "time"
    const val DATA_SENT = "data_up"
    const val DATA_RECEIVED = "data_received"
    const val WIFI_SENT = "wifi_sent"
    const val WIFI_RECEIVED = "wifi_received"
    const val NAME = "name"
    const val PACKAGE_NAME = "package_name"
    const val APP_SIZE = "app_size"
    const val INSTALL_DATE = "install_date"
    const val UPDATE_DATE = "update_date"
    const val TARGET_SDK = "target_sdk"

    fun ArrayList<PackageStats>.sortStats() {
        when (StatisticsPreferences.getSortedBy()) {
            NAME -> {
                sortByName()
            }
            DATA_SENT -> {
                sortByDataSent()
            }
            DATA_RECEIVED -> {
                sortByDataReceived()
            }
            WIFI_SENT -> {
                sortByWifiSent()
            }
            WIFI_RECEIVED -> {
                sortByWifiReceived()
            }
            TIME_USED -> {
                sortByTime()
            }
            PACKAGE_NAME -> {
                sortByPackageName()
            }
            APP_SIZE -> {
                sortBySize()
            }
            INSTALL_DATE -> {
                sortByInstallDate()
            }
            UPDATE_DATE -> {
                sortByUpdateDate()
            }
            TARGET_SDK -> {
                sortByTargetSdk()
            }
            else -> {
                sortByName()
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByName() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.packageInfo!!.applicationInfo.name.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.packageInfo!!.applicationInfo.name.lowercase(Locale.getDefault())
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByTime() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.totalTimeUsed
            }
        } else {
            this.sortBy {
                it.totalTimeUsed
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByDataSent() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.mobileData?.tx
            }
        } else {
            this.sortBy {
                it.mobileData?.tx
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByDataReceived() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.mobileData?.rx
            }
        } else {
            this.sortBy {
                it.mobileData?.rx
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByWifiSent() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.wifiData?.tx
            }
        } else {
            this.sortBy {
                it.wifiData?.tx
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByWifiReceived() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.wifiData?.rx
            }
        } else {
            this.sortBy {
                it.wifiData?.rx
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByPackageName() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.packageInfo!!.packageName.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.packageInfo!!.packageName.lowercase(Locale.getDefault())
            }
        }
    }

    private fun ArrayList<PackageStats>.sortBySize() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.appSize
            }
        } else {
            this.sortBy {
                it.appSize
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByInstallDate() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.packageInfo!!.firstInstallTime
            }
        } else {
            this.sortBy {
                it.packageInfo!!.firstInstallTime
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByUpdateDate() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.packageInfo!!.lastUpdateTime
            }
        } else {
            this.sortBy {
                it.packageInfo!!.lastUpdateTime
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByTargetSdk() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.packageInfo!!.applicationInfo.targetSdkVersion
            }
        } else {
            this.sortBy {
                it.packageInfo!!.applicationInfo.targetSdkVersion
            }
        }
    }
}