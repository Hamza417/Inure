package app.simple.inure.utils

import android.app.Activity
import android.util.Log

object GooglePlayUtils {

    private const val TAG = "GooglePlayUtils"

    fun Activity.showAppReview() {
        Log.i(TAG, "showAppReview: not available on this build variant.")
    }
}
