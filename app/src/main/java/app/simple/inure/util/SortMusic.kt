package app.simple.inure.util

import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.MusicPreferences

object SortMusic {

    /**
     * Sorts the music list by title
     */
    const val NAME = "name"

    /**
     * Sorts the music list by date
     */
    const val DATE = "date"

    fun ArrayList<AudioModel>.getSortedList(
            type: String = MusicPreferences.getMusicSort(),
            reverse: Boolean = MusicPreferences.getMusicSortReverse()): ArrayList<AudioModel> {
        when (type) {
            NAME -> {
                this.sortByName(reverse)
            }
            DATE -> {
                this.sortByDate(reverse)
            }
            else -> {
                MusicPreferences.setMusicSort(NAME)
                throw IllegalArgumentException("use default sorting constants to sort the list")
            }
        }

        return this
    }

    private fun ArrayList<AudioModel>.sortByName(reverse: Boolean) {
        if (reverse) {
            this.sortByDescending { it.name }
        } else {
            this.sortBy { it.name }
        }
    }

    private fun ArrayList<AudioModel>.sortByDate(reverse: Boolean) {
        if (reverse) {
            this.sortByDescending { it.dateAdded }
        } else {
            this.sortBy { it.dateAdded }
        }
    }
}