package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.isInstalled
import app.simple.inure.constants.DebloatSortConstants
import app.simple.inure.constants.SortConstant
import app.simple.inure.enums.Removal
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.Bloat
import app.simple.inure.models.PackageStateResult
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.preferences.DebloatPreferences
import app.simple.inure.shizuku.ShizukuUtils
import app.simple.inure.sort.DebloatSort.getSortedList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

class DebloatViewModel(application: Application) : RootShizukuViewModel(application) {

    private var currentMethod: String = METHOD_DISABLE // should change during runtime

    private val bloatList: MutableLiveData<ArrayList<Bloat>> by lazy {
        MutableLiveData<ArrayList<Bloat>>()
    }

    private val debloatedPackages: MutableLiveData<ArrayList<PackageStateResult>> by lazy {
        MutableLiveData<ArrayList<PackageStateResult>>()
    }

    fun getBloatList(): LiveData<ArrayList<Bloat>> {
        return bloatList
    }

    fun getDebloatedPackages(): LiveData<ArrayList<PackageStateResult>> {
        return debloatedPackages
    }

    fun shouldShowLoader(): Boolean {
        runCatching {
            return bloatList.value.isNullOrEmpty()
        }.getOrElse {
            return true
        }
    }

    private fun parseUADList() {
        viewModelScope.launch(Dispatchers.IO) {
            val uadList = getUADList()
            val apps = getInstalledApps() + getUninstalledApps()
            var bloats = ArrayList<Bloat>()

            uadList.parallelStream().forEach { bloat ->
                synchronized(bloats) {
                    apps.forEach { app ->
                        if (app.packageName == bloat.id) {
                            bloat.packageInfo = app
                            bloats.add(bloat)
                        }
                    }
                }
            }

            // Filter system or user apps
            when (DebloatPreferences.getApplicationType()) {
                SortConstant.SYSTEM -> {
                    bloats = bloats.parallelStream().filter { b ->
                        b.packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<Bloat>
                }
                SortConstant.USER -> {
                    bloats = bloats.parallelStream().filter { b ->
                        b.packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<Bloat>
                }
            }

            // Sort the bloat list
            bloats.getSortedList()

            // Apply filters
            bloats = bloats.applyListFilter()
            bloats = bloats.applyMethodsFilter()
            bloats = bloats.applyStateFilter()

            // Remove duplicates
            bloats = bloats.distinctBy { it.id } as ArrayList<Bloat>

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
    private fun getUADList(): ArrayList<Bloat> {
        val bufferedReader = BufferedReader(InputStreamReader(DebloatViewModel::class.java.getResourceAsStream(UAD_FILE_NAME)))
        val stringBuilder = StringBuilder()
        var line: String?
        while (bufferedReader.readLine().also { line = it } != null) {
            stringBuilder.append(line)
        }
        bufferedReader.close()

        val json = stringBuilder.toString()
        val jsonArray = org.json.JSONArray(json)
        val bloats = arrayListOf<Bloat>()

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

            bloats.add(bloat)
        }

        return bloats
    }

    fun refreshBloatList() {
        refreshPackageData()
    }

    private fun ArrayList<Bloat>.applyListFilter(): ArrayList<Bloat> {
        val listType = DebloatPreferences.getListType()
        val filteredList = ArrayList<Bloat>()

        parallelStream().forEach {
            if (FlagUtils.isFlagSet(listType, DebloatSortConstants.AOSP)) {
                if (it.list.lowercase() == "aosp") {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(listType, DebloatSortConstants.CARRIER)) {
                if (it.list.lowercase() == "carrier") {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(listType, DebloatSortConstants.GOOGLE)) {
                if (it.list.lowercase() == "google") {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(listType, DebloatSortConstants.MISC)) {
                if (it.list.lowercase() == "misc") {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(listType, DebloatSortConstants.OEM)) {
                if (it.list.lowercase() == "oem") {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(listType, DebloatSortConstants.PENDING)) {
                if (it.list.lowercase() == "pending") {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(listType, DebloatSortConstants.UNLISTED_LIST)) {
                if (it.list.lowercase() == "unlisted") {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }
        }

        return filteredList
    }

    private fun ArrayList<Bloat>.applyMethodsFilter(): ArrayList<Bloat> {
        val removalType = DebloatPreferences.getRemovalType()
        val filteredList = ArrayList<Bloat>()

        parallelStream().forEach {
            if (FlagUtils.isFlagSet(removalType, DebloatSortConstants.RECOMMENDED)) {
                if (it.removal == Removal.RECOMMENDED) {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(removalType, DebloatSortConstants.ADVANCED)) {
                if (it.removal == Removal.ADVANCED) {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(removalType, DebloatSortConstants.EXPERT)) {
                if (it.removal == Removal.EXPERT) {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(removalType, DebloatSortConstants.UNSAFE)) {
                if (it.removal == Removal.UNSAFE) {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(removalType, DebloatSortConstants.UNLISTED)) {
                if (it.removal == Removal.UNLISTED) {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }
        }

        return filteredList
    }

    private fun ArrayList<Bloat>.applyStateFilter(): ArrayList<Bloat> {
        val state = DebloatPreferences.getRemovalType()
        val filteredList = ArrayList<Bloat>()

        parallelStream().forEach {
            if (FlagUtils.isFlagSet(state, DebloatSortConstants.DISABLED)) {
                if (it.packageInfo.applicationInfo.enabled.not()) {
                    if (it.packageInfo.isInstalled()) {
                        synchronized(filteredList) {
                            if (filteredList.contains(it).invert()) {
                                filteredList.add(it)
                            }
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(state, DebloatSortConstants.ENABLED)) {
                if (it.packageInfo.applicationInfo.enabled) {
                    if (it.packageInfo.isInstalled()) {
                        synchronized(filteredList) {
                            if (filteredList.contains(it).invert()) {
                                filteredList.add(it)
                            }
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(state, DebloatSortConstants.UNINSTALLED)) {
                if (it.packageInfo.isInstalled().invert()) {
                    synchronized(filteredList) {
                        if (filteredList.contains(it).invert()) {
                            filteredList.add(it)
                        }
                    }
                }
            }
        }

        return filteredList
    }

    fun initDebloaterEngine(method: String) {
        currentMethod = method
        initializeCoreFramework()
    }

    fun startDebloating(method: String) {
        val selectedBloats = ArrayList<Bloat>()
        bloatList.value?.forEach {
            if (it.isSelected) {
                selectedBloats.add(it)
            }
        }

        if (ConfigurationPreferences.isUsingRoot()) {
            debloatRoot(selectedBloats, method)
        } else if (ConfigurationPreferences.isUsingShizuku()) {
            debloatShizuku(selectedBloats, method)
        }
    }

    override fun onShellCreated(shell: Shell?) {
        super.onShellCreated(shell)
        startDebloating(currentMethod)
    }

    override fun onShizukuCreated() {
        super.onShizukuCreated()
        startDebloating(currentMethod)
    }

    private fun debloatRoot(bloats: ArrayList<Bloat>, method: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val debloatedPackages = ArrayList<PackageStateResult>()
            val user = getCurrentUser()

            bloats.forEach {
                Shell.cmd(getCommand(method, user, it.id)).exec().let { result ->
                    if (result.isSuccess) {
                        debloatedPackages.add(PackageStateResult(it.packageInfo.applicationInfo.name, it.id, true))
                    } else {
                        debloatedPackages.add(PackageStateResult(it.packageInfo.applicationInfo.name, it.id, false))
                    }
                }
            }

            this@DebloatViewModel.debloatedPackages.postValue(debloatedPackages)
        }
    }

    private fun debloatShizuku(bloats: ArrayList<Bloat>, method: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val debloatedPackages = mutableSetOf<PackageStateResult>()
            val user = getCurrentUser()

            bloats.forEach { bloat ->
                kotlin.runCatching {
                    ShizukuUtils.execInternal(app.simple.inure.shizuku.Shell.Command(getCommand(method, user, bloat.id)), null).let { result ->
                        if (result.isSuccess) {
                            debloatedPackages.add(PackageStateResult(bloat.packageInfo.applicationInfo.name, bloat.id, true))
                        } else {
                            debloatedPackages.add(PackageStateResult(bloat.packageInfo.applicationInfo.name, bloat.id, false))
                        }
                    }
                }.onSuccess {
                    debloatedPackages.add(PackageStateResult(bloat.packageInfo.applicationInfo.name, bloat.id, true))
                }.onFailure {
                    debloatedPackages.add(PackageStateResult(bloat.packageInfo.applicationInfo.name, bloat.id, false))
                }.getOrElse {
                    debloatedPackages.add(PackageStateResult(bloat.packageInfo.applicationInfo.name, bloat.id, false))
                }
            }
        }
    }

    private fun getCurrentUser(): Int {
        kotlin.runCatching {
            var user = 0
            if (ConfigurationPreferences.isUsingRoot()) {
                Shell.cmd("am get-current-user").exec().let { result ->
                    if (result.isSuccess) {
                        user = result.out.joinToString().toInt()
                    }
                }
            } else if (ConfigurationPreferences.isUsingShizuku()) {
                kotlin.runCatching {
                    ShizukuUtils.execInternal(app.simple.inure.shizuku.Shell.Command("am get-current-user"), null)
                }.onSuccess {
                    user = it.out.toInt()
                }.onFailure {
                    postError(it)
                }
            }

            return user
        }.onFailure {
            postError(it)
        }

        return 0
    }

    private fun getCommand(method: String, user: Int, appID: String): String {
        return when (method) {
            METHOD_DISABLE -> "pm disable --user $user $appID"
            METHOD_UNINSTALL -> "pm uninstall --user $user $appID"
            METHOD_RESTORE -> "pm install-existing --user $user $appID && pm enable --user $user $appID"
            else -> throw IllegalArgumentException("Invalid method")
        }
    }

    fun clearDebloatedPackages() {
        debloatedPackages.postValue(null)
    }

    companion object {
        private const val UAD_FILE_NAME = "/uad_lists.json"
        const val METHOD_DISABLE = "disable"
        const val METHOD_UNINSTALL = "uninstall"
        const val METHOD_RESTORE = "restore"
    }
}
