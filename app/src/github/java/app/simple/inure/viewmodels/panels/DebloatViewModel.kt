package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.enums.Removal
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.Bloat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class DebloatViewModel(application: Application) : PackageUtilsViewModel(application) {

    private val bloatList: MutableLiveData<ArrayList<Bloat>> by lazy {
        MutableLiveData<ArrayList<Bloat>>()
    }

    fun getBloatList(): LiveData<ArrayList<Bloat>> {
        return bloatList
    }

    private fun parseUADList() {
        viewModelScope.launch(Dispatchers.IO) {
            val uadList = getUADList()
            val apps = getInstalledApps()
            val bloats = ArrayList<Bloat>()

            uadList.forEach { (id, bloat) ->
                apps.forEach { app ->
                    if (app.packageName == id) {
                        bloat.packageInfo = app
                        bloats.add(bloat)
                    }
                }
            }

            bloatList.postValue(bloats)
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        parseUADList()
    }

    /**
     * {
     *     "id": "com.android.package",
     *     "list": "Oem",
     *     "description": "desc \n",
     *     "dependencies": [],
     *     "neededBy": [],
     *     "labels": [],
     *     "removal": "Recommended"
     *   },
     */
    fun getUADList(): HashMap<String, Bloat> {
        val bufferedReader = BufferedReader(InputStreamReader(DebloatViewModel::class.java.getResourceAsStream(UAD_FILE_NAME)))
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        bufferedReader.close()

        val json = stringBuilder.toString()
        val jsonArray = org.json.JSONArray(json)
        val bloats = hashMapOf<String, Bloat>()

        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val id = jsonObject.getString("id")
            val list = jsonObject.getString("list")
            val description = jsonObject.getString("description")
            val removal = jsonObject.getString("removal")
            val dependencies = jsonObject.getJSONArray("dependencies")
            val neededBy = jsonObject.getJSONArray("neededBy")
            val labels = jsonObject.getJSONArray("labels")

            val bloat = Bloat()
            bloat.id = id
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

            bloats[id] = bloat
        }

        return bloats
    }

    companion object {
        private const val UAD_FILE_NAME = "/uad_lists.json"
    }
}