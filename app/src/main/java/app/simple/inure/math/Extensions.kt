package app.simple.inure.math

import kotlin.math.roundToLong

object Extensions {
    fun Float.round1(): Float = (this * 10.0).roundToLong() / 10.0f

    fun Double.round1(): Double = (this * 10.0).roundToLong() / 10.0

    fun Float.round2(): Float = (this * 100.0).roundToLong() / 100.0f

    fun Double.round2(): Double = (this * 100.0).roundToLong() / 100.0

    fun Long.percentOf(total: Long): Float = (this * 100.0 / total).toFloat()

    fun Int.percentOf(total: Int): Float = (this * 100.0 / total).toFloat()

    fun Float.percentOf(total: Float): Float = (this * 100.0 / total).toFloat()

    fun Double.percentOf(total: Double): Double = (this * 100.0 / total)

    fun Float.getPercentPart(total: Float): Float = (this * total / 100.0).toFloat()
}