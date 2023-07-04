package app.simple.inure.util

import android.content.Context
import app.simple.inure.R
import app.simple.inure.util.ConditionUtils.invert

object TrackerUtils {
    fun Context.getTrackerSignatures(): List<String> {
        val trackers = resources.getStringArray(R.array.trackers).filter {
            it.isNullOrEmpty().invert()
        }

        val oldTrackers = resources.getStringArray(R.array.old_trackers).filter {
            it.isNullOrEmpty().invert()
        }

        return (trackers + oldTrackers).distinct()
    }
}