package app.simple.inure.viewmodels.panels

import android.app.Application
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.popups.apks.PopupApksCategory
import app.simple.inure.preferences.ApkBrowserPreferences
import app.simple.inure.util.SortApks.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class ApkBrowserViewModel(application: Application) : WrappedViewModel(application) {

    private var files = ArrayList<File>()

    private val pathData: MutableLiveData<ArrayList<File>> by lazy {
        MutableLiveData<ArrayList<File>>().also {
            loadApkPaths()
        }
    }

    private val info: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getApkFiles(): LiveData<ArrayList<File>> {
        return pathData
    }

    fun getPathInfo(): LiveData<String> {
        return info
    }

    private fun loadApkPaths() {
        viewModelScope.launch(Dispatchers.IO) {
            val externalStorage = Environment.getExternalStorageDirectory()
            val apkPaths = ArrayList<File>()

            externalStorage.walkTopDown().forEach {
                info.postValue(it.absolutePath.substringBeforeLast("/"))

                when (ApkBrowserPreferences.getAppsCategory()) {
                    PopupApksCategory.APK -> {
                        if (it.isFile && it.extension == "apk") {
                            apkPaths.add(it)
                        }
                    }
                    PopupApksCategory.SPLIT -> {
                        if (it.isFile && it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk") {
                            apkPaths.add(it)
                        }
                    }
                    PopupApksCategory.BOTH -> {
                        if (it.isFile && it.extension == "apk" || it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk") {
                            apkPaths.add(it)
                        }
                    }
                }

                if (it.isFile && it.extension == "apk" || it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk") {
                    files.add(it) // backup for filtering
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
            val filteredPaths = ArrayList<File>()

            files.forEach {
                when (ApkBrowserPreferences.getAppsCategory()) {
                    PopupApksCategory.APK -> {
                        if (it.isFile && it.extension == "apk" && it.name.contains(query, true)) {
                            filteredPaths.add(it)
                        }
                    }
                    PopupApksCategory.SPLIT -> {
                        if (it.isFile && it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk" && it.name.contains(query, true)) {
                            filteredPaths.add(it)
                        }
                    }
                    PopupApksCategory.BOTH -> {
                        if (it.isFile && it.extension == "apk" || it.extension == "apks" || it.extension == "apkm" || it.extension == "xapk" && it.name.contains(query, true)) {
                            filteredPaths.add(it)
                        }
                    }
                }
            }

            filteredPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            pathData.postValue(filteredPaths.clone() as ArrayList<File>)
        }
    }

    fun sort() {
        viewModelScope.launch(Dispatchers.IO) {
            val sortedPaths = ArrayList<File>()

            pathData.value?.forEach {
                sortedPaths.add(it)
            }

            sortedPaths.getSortedList(ApkBrowserPreferences.getSortStyle(), ApkBrowserPreferences.isReverseSorting())

            @Suppress("UNCHECKED_CAST")
            pathData.postValue(sortedPaths.clone() as ArrayList<File>)
        }
    }
}