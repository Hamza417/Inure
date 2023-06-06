package app.simple.inure.constants

import android.graphics.Color

object Colors {

    private val pastel: ArrayList<Int> by lazy {
        arrayListOf(
                Color.parseColor("#A7727D"),
                Color.parseColor("#6096B4"),
                Color.parseColor("#D0B8A8"),
                0,
                Color.parseColor("#8B7E74"),
                Color.parseColor("#7895B2"),
                Color.parseColor("#967E76"),
                Color.parseColor("#7D9D9C"),
                0,
                Color.parseColor("#748DA6"),
                Color.parseColor("#576F72"),
                Color.parseColor("#68A7AD"),
                Color.parseColor("#9A86A4"),
                Color.parseColor("#655D8A"),
                0,
                Color.parseColor("#7C99AC"),
                Color.parseColor("#8E806A"),
                Color.parseColor("#96C7C1"),
                Color.parseColor("#87AAAA"),
                Color.parseColor("#9D9D9D"),
                0,
        )
    }

    fun getPastelColor(): ArrayList<Int> {
        return pastel
    }
}