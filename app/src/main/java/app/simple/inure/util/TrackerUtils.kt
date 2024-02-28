package app.simple.inure.util

import android.content.Context
import android.util.Log
import app.simple.inure.R
import app.simple.inure.models.Tracker
import app.simple.inure.util.ArrayUtils.toStringArray
import app.simple.inure.util.ConditionUtils.invert
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

object TrackerUtils {

    private const val TRACKERS_JSON = "/trackers.json"

    fun Context.getTrackerSignatures(): List<String> {
        val trackers = resources.getStringArray(R.array.trackers).filter {
            it.isNullOrEmpty().invert()
        }

        val oldTrackers = resources.getStringArray(R.array.old_trackers).filter {
            it.isNullOrEmpty().invert()
        }

        return (trackers + oldTrackers).distinct()
    }

    /**
     * {
     *     "trackers": {
     *         "1": {
     *             "categories": [
     *                 "Analytics"
     *             ],
     *             "code_signature": "com.databerries.|com.geolocstation.",
     *             "creation_date": "2017-09-24",
     *             "description": "",
     *             "documentation": [],
     *             "id": 1,
     *             "name": "Teemo",
     *             "network_signature": "databerries\\.com",
     *             "website": "https://www.teemo.co"
     *         },
     *     }
     *  }
     */
    fun getTrackersData(): ArrayList<Tracker> {
        ProcessUtils.ensureNotOnMainThread {
            val bufferedReader = BufferedReader(InputStreamReader(
                    TrackerUtils::class.java.getResourceAsStream(TRACKERS_JSON)))
            val stringBuilder = StringBuilder()
            var line: String?
            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            val json = stringBuilder.toString()
            val jsonObject = JSONObject(json)
            val trackers = jsonObject.getJSONObject("trackers")
            val trackersList = arrayListOf<Tracker>()

            val keysIterator = trackers.keys()

            while (keysIterator.hasNext()) {
                val key = keysIterator.next()
                val tracker = trackers.getJSONObject(key)
                val name = tracker.getString("name")
                val codeSignature = tracker.getString("code_signature")
                val networkSignature = tracker.getString("network_signature")
                val website = tracker.getString("website")
                val creationDate = tracker.getString("creation_date")
                val description = tracker.getString("description")
                val categories = tracker.getJSONArray("categories")
                val documentation = tracker.getJSONArray("documentation")

                val trackerObject = Tracker()
                trackerObject.name = name
                trackerObject.codeSignature = codeSignature
                trackerObject.networkSignature = networkSignature
                trackerObject.website = website
                trackerObject.creationDate = creationDate
                trackerObject.description = description
                Log.d("TrackerUtils", "getTrackersData: loading categories: $categories")
                trackerObject.categories = categories.toStringArray()
                trackerObject.documentation = documentation.toStringArray()

                trackersList.add(trackerObject)
            }

            return trackersList
        }
    }
}