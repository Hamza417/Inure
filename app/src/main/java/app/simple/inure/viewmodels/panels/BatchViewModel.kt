package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.database.instances.BatchDatabase
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.models.BatchModel
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.popups.apps.PopupAppsCategory
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class BatchViewModel(application: Application) : WrappedViewModel(application) {

    private var batchDatabase: BatchDatabase? = null

    private val appData: MutableLiveData<ArrayList<BatchPackageInfo>> by lazy {
        MutableLiveData<ArrayList<BatchPackageInfo>>().also {
            loadAppData()
        }
    }

    fun getAppData(): LiveData<ArrayList<BatchPackageInfo>> {
        return appData
    }

    private fun loadAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList

            when (MainPreferences.getAppsCategory()) {
                PopupAppsCategory.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                PopupAppsCategory.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            for (i in apps.indices) {
                apps[i].applicationInfo.name = PackageUtils.getApplicationName(getApplication<Application>().applicationContext, apps[i].applicationInfo)
            }

            apps.getSortedList(MainPreferences.getSortStyle(), MainPreferences.isReverseSorting())

            appData.postValue(getBatchStateData(apps))
        }
    }

    private fun getBatchStateData(apps: ArrayList<PackageInfo>): ArrayList<BatchPackageInfo> {
        batchDatabase = BatchDatabase.getInstance(context)

        val list = arrayListOf<BatchPackageInfo>()

        for (app in apps) {
            list.add(BatchPackageInfo(app, false, -1))
        }

        for (batch in batchDatabase!!.batchDao()!!.getBatch()) {
            for (item in list) {
                if (batch.packageName == item.packageInfo.packageName) {
                    with(batch.isSelected) {
                        item.isSelected = this
                        if (this) {
                            item.dateSelected = batch.dateSelected
                        } else {
                            item.dateSelected = -1
                        }
                    }
                    break
                }
            }
        }

        if (BatchPreferences.isSelectionOnTop()) {
            list.sortByDescending {
                it.isSelected
            }
        }

        return list
    }

    fun updateBatchItem(batchPackageInfo: BatchPackageInfo) {
        viewModelScope.launch(Dispatchers.IO) {
            batchDatabase = BatchDatabase.getInstance(context)
            batchDatabase?.batchDao()
                ?.insertBatch(BatchModel(batchPackageInfo.packageInfo.packageName,
                                         batchPackageInfo.isSelected,
                                         System.currentTimeMillis()))
        }
    }

    override fun onCleared() {
        super.onCleared()
        batchDatabase?.close()
    }
}