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
}