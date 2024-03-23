package app.simple.inure.apk.utils

object SearchUtils {

    private fun <T> ArrayList<T>.getMatchedCount(search: String): Int {
        var count = 0
        for (item in this) {
            if (item.toString().contains(search, true)) {
                count++
            }
        }

        return count
    }
}
