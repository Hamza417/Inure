package app.simple.inure.utils

import android.app.Activity
import android.util.Log
import app.simple.inure.preferences.MainPreferences
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory

object GooglePlayUtils {

    private const val TAG = "GooglePlayUtils"

    fun Activity.showAppReview() {
        val manager: ReviewManager = ReviewManagerFactory.create(this)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result
                val flow = manager.launchReviewFlow(this, reviewInfo)
                flow.addOnCompleteListener { _ ->
                    // The flow has finished. The API does not indicate whether the user
                    // reviewed or not, or even whether the review dialog was shown. Thus, no
                    // matter the result, we continue our app flow.
                    Log.i(TAG, "review flow complete.")
                }

                flow.addOnFailureListener { e ->
                    Log.e(TAG, "failed to launch review flow: $e")
                }

                flow.addOnCanceledListener {
                    Log.i(TAG, "user cancelled review flow.")
                    MainPreferences.setShowRateReminder(false)
                }
            }
        }
    }
}
