package app.simple.inure.viewmodels.panels

import android.app.Application
import android.os.Environment
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.SortConstant
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ApkFile
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.SDCard
import app.simple.inure.util.SortApks.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

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

    fun shouldShowLoader(): Boolean {
        return pathData.value.isNullOrEmpty()
    }

    private fun loadApkPaths() {
        viewModelScope.launch(Dispatchers.IO) {
            val externalStoragePaths: ArrayList<File?> = if (ApkBrowserPreferences.isExternalStorage()) {
                arrayListOf(Environment.getExternalStorageDirectory(), SDCard.findSdCardPath(application))
            } else {
                arrayListOf(Environment.getExternalStorageDirectory())
            }

            val apkPaths = ArrayList<ApkFile>()
            files.clear()

            externalStoragePaths.forEach { path ->
                path?.walkTopDown()!!.forEach {
                    info.postValue(it.absolutePath.substringBeforeLast("/"))

                    if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APK) && it.extension == "apk") {
                        apkPaths.add(ApkFile(it))
                    }

                    if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKS) && it.extension == "apks") {
                        apkPaths.add(ApkFile(it))
                    }

                    if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKM) && it.extension == "apkm") {
                        apkPaths.add(ApkFile(it))
                    }

                    if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_XAPK) && it.extension == "xapk") {
                        apkPaths.add(ApkFile(it))
                    }

                    if (it.isFile && it.extension == "apk" || it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk") {
                        files.add(ApkFile(it)) // backup for filtering
                    }
                }
            }

            if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_HIDDEN).invert()) {
                val mediaPaths = ArrayList<ApkFile>()

                for (file in apkPaths) {
                    if (file.file.absolutePath.split("/").any { it.startsWith(".") }.invert()) {
                        mediaPaths.add(file)
                    }
                }

                apkPaths.clear()

                for (file in mediaPaths) {
                    apkPaths.add(file)
                }
            }

            apkPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            pathData.postValue(apkPaths)
        }
    }

    fun refresh() {
        loadApkPaths()
    }

    /**
     * This is a terrible way to check if a file is a .nomedia file or directory
     */
    @Suppress("unused")
    private fun File.isNomediaFileOrDirectory(): Boolean {
        return if (isFile) {
            if (absolutePath.split("/").any { it.startsWith(".") }) {
                return true
            } else {
                if (name == ".nomedia") {
                    return true
                } else {
                    for (i in 0..parentFile?.listFiles()?.size!!.minus(1)) {
                        if (parentFile?.listFiles()?.get(i)?.name == ".nomedia") {
                            Log.d("File", "Found .nomedia file in ${parentFile?.listFiles()?.get(i)?.absolutePath}")
                            return true
                        }
                    }

                    return false
                }
            }
        } else if (isDirectory) {
            if (absolutePath.split("/").any { it.startsWith(".") }) {
                return true
            } else {
                listFiles()?.forEach {
                    if (it.name == ".nomedia") {
                        return true
                    }
                }
            }
            false
        } else {
            false
        }
    }

    fun filter() {
        viewModelScope.launch(Dispatchers.IO) {
            val filteredPaths = ArrayList<ApkFile>()

            files.forEach {
                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APK) && it.file.extension == "apk") {
                    filteredPaths.add(it)
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKS) && it.file.extension == "apks") {
                    filteredPaths.add(it)
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKM) && it.file.extension == "apkm") {
                    filteredPaths.add(it)
                }

                if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_XAPK) && it.file.extension == "xapk") {
                    filteredPaths.add(it)
                }
            }

            if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_HIDDEN).invert()) {
                val mediaPaths = ArrayList<ApkFile>()

                for (file in filteredPaths) {
                    if (file.file.absolutePath.split("/").any { it.startsWith(".") }.invert()) {
                        mediaPaths.add(file)
                        Log.d("ApkBrowserViewModel", "loadApkPaths: ${file.file.absolutePath} : is not nomedia file")
                    }
                }

                filteredPaths.clear()

                for (file in mediaPaths) {
                    filteredPaths.add(file)
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
                        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APK) && it.file.extension == "apk") {
                            filteredPaths.add(it)
                        }

                        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKS) && it.file.extension == "apks") {
                            filteredPaths.add(it)
                        }

                        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_APKM) && it.file.extension == "apkm") {
                            filteredPaths.add(it)
                        }

                        if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_XAPK) && it.file.extension == "xapk") {
                            filteredPaths.add(it)
                        }
                    }
                }
            }

            if (FlagUtils.isFlagSet(ApkBrowserPreferences.getApkFilter(), SortConstant.APKS_HIDDEN).invert()) {
                val mediaPaths = ArrayList<ApkFile>()

                for (file in filteredPaths) {
                    if (file.file.absolutePath.split("/").any { it.startsWith(".") }.invert()) {
                        mediaPaths.add(file)
                        Log.d("ApkBrowserViewModel", "loadApkPaths: ${file.file.absolutePath} : is not nomedia file")
                    }
                }

                filteredPaths.clear()

                for (file in mediaPaths) {
                    filteredPaths.add(file)
                }
            }

            filteredPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            searchData.postValue(filteredPaths.clone() as ArrayList<ApkFile>)
        }
    }

    fun delete(file: ApkFile) {
        files.remove(file)
    }
}
