package app.simple.inure.math

object Range {
    fun Float.mapRange(fromMin: Float, fromMax: Float, toMin: Float, toMax: Float): Float {
        return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
    }

    fun Float.mapRange(fromMin: Int, fromMax: Int, toMin: Int, toMax: Int): Float {
        return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
    }

    fun Double.mapRange(fromMin: Double, fromMax: Double, toMin: Double, toMax: Double): Double {
        return (this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin
    }

    fun Int.mapRange(fromMin: Int, fromMax: Int, toMin: Int, toMax: Int): Int {
        return ((this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin)
    }

    fun Long.mapRange(fromMin: Long, fromMax: Long, toMin: Long, toMax: Long): Long {
        return ((this - fromMin) / (fromMax - fromMin) * (toMax - toMin) + toMin)
    }

    fun Float.normalize(min: Float, max: Float): Float {
        return (this - min) / (max - min)
    }
}
