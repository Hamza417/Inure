package app.simple.inure.models

data class ExitReason(
        val reason: Int,
        val details: String,
        val timestamp: Long
)