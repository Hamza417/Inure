package app.simple.inure.util

import android.content.pm.ApplicationInfo
import java.util.*
import kotlin.collections.ArrayList

object Sort {

    const val ALPHABETICALLY = "alphabetically"
    const val REVERSED_ALPHABETICALLY = "reversed_alphabetically"

    /**
     * Sort the given [ArrayList] in various ways
     *
     * @param type use [Sort.ALPHABETICALLY] constants
     *             to specify sorting methods for the list
     *
     * @param reversed inverts the list after sorting
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun ArrayList<ApplicationInfo>.getSortedList(type: String, reversed: Boolean) {
        when (type) {
            ALPHABETICALLY -> {
                this.sortAlphabetically()
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * sort application list alphabetically
     */
    private fun ArrayList<ApplicationInfo>.sortAlphabetically() {
        return this.sortBy { it.name.toUpperCase(Locale.getDefault()) }
    }
}