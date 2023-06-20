package app.simple.inure.viewmodels.panels

import android.app.Application
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ApkFile
import app.simple.inure.popups.apks.PopupApksCategory
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.SortApks.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApkBrowserViewModel(application: Application) : WrappedViewModel(application) {

    private var files = ArrayList<ApkFile>()

    private val pathData: MutableLiveData<ArrayList<ApkFile>> by lazy {
        MutableLiveData<ArrayList<ApkFile>>().also {
            loadApkPaths()
        }
    }

    private val searchData: MutableLiveData<ArrayList<ApkFile>> by lazy {
        MutableLiveData<ArrayList<ApkFile>>().also {
            search(ApkBrowserPreferences.getSearchKeyword())
        }
    }

    private val info: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getApkFiles(): LiveData<ArrayList<ApkFile>> {
        return pathData
    }

    fun getSearchResults(): LiveData<ArrayList<ApkFile>> {
        return searchData
    }

    fun getPathInfo(): LiveData<String> {
        return info
    }

    private fun loadApkPaths() {
        viewModelScope.launch(Dispatchers.IO) {
            val externalStorage = Environment.getExternalStorageDirectory()
            val apkPaths = ArrayList<ApkFile>()

            externalStorage.walkTopDown().forEach {
                info.postValue(it.absolutePath.substringBeforeLast("/"))

                when (ApkBrowserPreferences.getAppsCategory()) {
                    PopupApksCategory.APK -> {
                        if (it.isFile && it.extension == "apk") {
                            apkPaths.add(ApkFile(it))
                        }
                    }
                    PopupApksCategory.SPLIT -> {
                        if (it.isFile && it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk") {
                            apkPaths.add(ApkFile(it))
                        }
                    }
                    PopupApksCategory.BOTH -> {
                        if (it.isFile && it.extension == "apk" || it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk") {
                            apkPaths.add(ApkFile(it))
                        }
                    }
                }

                if (it.isFile && it.extension == "apk" || it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk") {
                    files.add(ApkFile(it)) // backup for filtering
                }
            }

            apkPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            pathData.postValue(apkPaths)
        }
    }

    fun refresh() {
        loadApkPaths()
    }

    fun filter(query: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val filteredPaths = ArrayList<ApkFile>()

            files.forEach {
                when (ApkBrowserPreferences.getAppsCategory()) {
                    PopupApksCategory.APK -> {
                        if (it.file.isFile && it.file.extension == "apk" && it.file.name.contains(query, true)) {
                            filteredPaths.add(ApkFile(it.file))
                        }
                    }
                    PopupApksCategory.SPLIT -> {
                        if (it.file.isFile && it.file.extension == "apks" || it.file.extension == "apkm" || it.file.extension == "xapk" && it.file.name.contains(query, true)) {
                            filteredPaths.add(it)
                        }
                    }
                    PopupApksCategory.BOTH -> {
                        if (it.file.isFile && it.file.extension == "apk" || it.file.extension == "apks" || it.file.extension == "apkm" || it.file.extension == "xapk" && it.file.name.contains(query, true)) {
                            filteredPaths.add(it)
                        }
                    }
                }
            }

            filteredPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            pathData.postValue(filteredPaths.clone() as ArrayList<ApkFile>)
        }
    }

    fun sort() {
        viewModelScope.launch(Dispatchers.IO) {
            val sortedPaths = ArrayList<ApkFile>()

            pathData.value?.forEach {
                sortedPaths.add(it)
            }

            sortedPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            pathData.postValue(sortedPaths.clone() as ArrayList<ApkFile>)
        }
    }

    fun search(keyword: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val filteredPaths = ArrayList<ApkFile>()

            if (keyword.isEmpty()) {
                @Suppress("UNCHECKED_CAST")
                searchData.postValue(filteredPaths.clone() as ArrayList<ApkFile>)
                return@launch
            }

            if (keyword.startsWith("$")) {
                when (keyword.lowercase()) {
                    "\$apk" -> {
                        files.forEach {
                            if (it.file.extension == "apk") {
                                filteredPaths.add(it)
                            }
                        }
                    }
                    "\$apks" -> {
                        files.forEach {
                            if (it.file.extension == "apks") {
                                filteredPaths.add(it)
                            }
                        }
                    }
                    "\$apkm" -> {
                        files.forEach {
                            if (it.file.extension == "apkm") {
                                filteredPaths.add(it)
                            }
                        }
                    }
                    "\$xapk" -> {
                        files.forEach {
                            if (it.file.extension == "xapk") {
                                filteredPaths.add(it)
                            }
                        }
                    }
                }
            } else {
                files.forEach {
                    if (it.file.name.contains(keyword, true) ||
                        it.file.absolutePath.contains(keyword, true) ||
                        it.file.extension.contains(keyword, true) ||
                        it.file.lastModified().toDate().contains(keyword, true)) {
                        filteredPaths.add(it)
                    }
                }
            }

            filteredPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            searchData.postValue(filteredPaths.clone() as ArrayList<ApkFile>)
        }
    }
}