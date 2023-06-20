package app.simple.inure.util

import app.simple.inure.models.ApkFile
import java.io.File
import java.util.*

object SortApks {

    /**
     * Sorts the [File] [ArrayList] by name
     */
    const val NAME = "name"

    /**
     * Sorts the [File] [ArrayList] by size
     */
    const val SIZE = "size"

    /**
     * Sorts the [File] [ArrayList] by date
     */
    const val DATE = "date"

    /**
     * Sort the given [ArrayList] in various ways
     *
     * @param type use [SortApks.NAME] constants
     *             to specify sorting methods for the list
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun ArrayList<ApkFile>.getSortedList(type: String, reverse: Boolean) {
        when (type) {
            NAME -> {
                this.sortByName(reverse)
            }
            SIZE -> {
                this.sortBySize(reverse)
            }
            DATE -> {
                this.sortByCreatedOn(reverse)
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * Sort the given [ArrayList] in various ways
     *
     * @param type use [SortApks.NAME] constants
     *             to specify sorting methods for the list
     *
     * @throws IllegalArgumentException if the [type] parameter
     *                                  is specified correctly
     */
    fun MutableList<ApkFile>.getSortedList(type: String, reverse: Boolean) {
        when (type) {
            NAME -> {
                (this as ArrayList).sortByName(reverse)
            }
            SIZE -> {
                (this as ArrayList).sortBySize(reverse)
            }
            DATE -> {
                (this as ArrayList).sortByCreatedOn(reverse)
            }
            else -> {
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }
    }

    /**
     * sort application list name
     */
    private fun ArrayList<ApkFile>.sortByName(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.file.name.lowercase(Locale.getDefault())
            }
        } else {
            this.sortBy {
                it.file.name.lowercase(Locale.getDefault())
            }
        }
    }

    /**
     * sort application list package name
     */
    private fun ArrayList<ApkFile>.sortBySize(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                it.file.length()
            }
        } else {
            this.sortBy {
                it.file.length()
            }
        }
    }

    /**
     * Sort by created on date
     */
    private fun ArrayList<ApkFile>.sortByCreatedOn(reverse: Boolean) {
        return if (reverse) {
            this.sortByDescending {
                //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //                    val basicFileAttributes = Files.readAttributes(it.toPath(), BasicFileAttributes::class.java)
                //                    basicFileAttributes.creationTime().toMillis()
                //                } else {
                //                    it.lastModified()
                //                }

                it.file.lastModified()
            }
        } else {
            this.sortBy {
                //                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //                    val basicFileAttributes = Files.readAttributes(it.toPath(), BasicFileAttributes::class.java)
                //                    basicFileAttributes.creationTime().toMillis()
                //                } else {
                //                    it.lastModified()
                //                }

                it.file.lastModified()
            }
        }
    }
}