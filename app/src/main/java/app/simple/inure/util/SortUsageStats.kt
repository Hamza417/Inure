package app.simple.inure.util

import app.simple.inure.models.PackageStats
import app.simple.inure.preferences.StatisticsPreferences
import java.util.*

object SortUsageStats {
    const val TIME = "time"
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
            TIME -> {
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
                it.dataSent
            }
        } else {
            this.sortBy {
                it.dataSent
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByDataReceived() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.dataReceived
            }
        } else {
            this.sortBy {
                it.dataReceived
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByWifiSent() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.dataSentWifi
            }
        } else {
            this.sortBy {
                it.dataSentWifi
            }
        }
    }

    private fun ArrayList<PackageStats>.sortByWifiReceived() {
        return if (StatisticsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.dataReceivedWifi
            }
        } else {
            this.sortBy {
                it.dataReceivedWifi
            }
        }
    }
}