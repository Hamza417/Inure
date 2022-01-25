package app.simple.inure.math

import kotlin.math.roundToLong

object Extensions {
    fun Float.round1(): Float = (this * 10.0).roundToLong() / 10.0f

    fun Double.round1(): Double = (this * 10.0).roundToLong() / 10.0

    fun Float.round2(): Float = (this * 100.0).roundToLong() / 100.0f

    fun Double.round2(): Double = (this * 100.0).roundToLong() / 100.0
}