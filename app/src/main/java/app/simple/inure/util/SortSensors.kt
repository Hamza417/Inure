package app.simple.inure.util

import android.hardware.Sensor
import app.simple.inure.preferences.SensorsPreferences
import java.util.*

object SortSensors {

    const val NAME = "NAME"
    const val POWER = "POWER"
    const val MAX_RANGE = "MAX_RANGE"
    const val RESOLUTION = "RESOLUTION"

    /**
     * Sort the given [ArrayList] in various ways
     *
     * @param type use constants
     *             to specify sorting methods for the list
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun MutableList<Sensor>.getSortedList(type: String) {
        when (type) {
            NAME -> {
                this.sortByName()
            }
            MAX_RANGE -> {
                this.sortByMaximumRange()
            }
            POWER -> {
                this.sortByPower()
            }
            RESOLUTION -> {
                this.sortByResolution()
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * sort [Sensor] by [Sensor.getName]
     */
    private fun MutableList<Sensor>.sortByName() {
        return if (SensorsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.name.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.name.lowercase(Locale.getDefault())
            }
        }
    }

    /**
     * sort [Sensor] by [Sensor.getPower]
     */
    private fun MutableList<Sensor>.sortByPower() {
        return if (SensorsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.power
            }
        } else {
            this.sortBy {
                it.power
            }
        }
    }

    /**
     * sort [Sensor] by [Sensor.getMaximumRange]
     */
    private fun MutableList<Sensor>.sortByMaximumRange() {
        return if (SensorsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.maximumRange
            }
        } else {
            this.sortBy {
                it.maximumRange
            }
        }
    }

    /**
     * sort [Sensor] by [Sensor.getResolution]
     */
    private fun MutableList<Sensor>.sortByResolution() {
        return if (SensorsPreferences.isReverseSorting()) {
            this.sortByDescending {
                it.resolution
            }
        } else {
            this.sortBy {
                it.resolution
            }
        }
    }
}