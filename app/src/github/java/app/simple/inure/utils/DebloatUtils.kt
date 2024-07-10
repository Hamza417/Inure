package app.simple.inure.utils

import android.content.pm.PackageInfo
import app.simple.inure.enums.Removal
import app.simple.inure.models.Bloat
import app.simple.inure.viewmodels.panels.DebloatViewModel
import java.io.BufferedReader
import java.io.InputStreamReader

object DebloatUtils {

    private const val UAD_FILE_NAME = "/uad_lists.json"

    private var bloatApps: Set<String> = setOf()

    fun getDebloatList(): ArrayList<Bloat> {
        val bufferedReader = BufferedReader(InputStreamReader(DebloatViewModel::class.java.getResourceAsStream(UAD_FILE_NAME)))
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        bufferedReader.close()
        val json = stringBuilder.toString()
        val jsonObject = org.json.JSONObject(json)
        val bloats = arrayListOf<Bloat>()

        jsonObject.keys().forEach { key ->
            val properties = jsonObject.getJSONObject(key)
            val list = properties.getString("list")
            val description = properties.getString("description")
            val removal = properties.getString("removal")
            val dependencies = properties.getJSONArray("dependencies")
            val neededBy = properties.getJSONArray("neededBy")
            val labels = properties.getJSONArray("labels")

            val bloat = Bloat()
            bloat.id = key // use the key as the id
            bloat.list = list
            bloat.description = description
            bloat.removal = Removal.valueOf(removal.uppercase())
            bloat.dependencies = ArrayList()
            bloat.neededBy = ArrayList()
            bloat.labels = ArrayList()

            for (j in 0 until dependencies.length()) {
                bloat.dependencies.add(dependencies.getString(j))
            }

            for (j in 0 until neededBy.length()) {
                bloat.neededBy.add(neededBy.getString(j))
            }

            for (j in 0 until labels.length()) {
                bloat.labels.add(labels.getString(j))
            }

            bloats.add(bloat)
        }

        return bloats
    }

    private fun initBloatAppsSet(bloats: ArrayList<Bloat>) {
        bloatApps = bloats.map { it.id }.toSet()
    }

    fun initBloatAppsSet() {
        initBloatAppsSet(getDebloatList())
    }

    fun PackageInfo.isPackageBloat(): Boolean {
        return bloatApps.contains(packageName)
    }
}
