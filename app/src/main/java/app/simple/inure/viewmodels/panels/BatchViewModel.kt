package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.SortConstant
import app.simple.inure.database.instances.BatchDatabase
import app.simple.inure.extensions.viewmodels.DataGeneratorViewModel
import app.simple.inure.models.BatchModel
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.preferences.BatchPreferences
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class BatchViewModel(application: Application) : DataGeneratorViewModel(application) {

    private var batchDatabase: BatchDatabase? = null

    private val batchData: MutableLiveData<ArrayList<BatchPackageInfo>> by lazy {
        MutableLiveData<ArrayList<BatchPackageInfo>>()
    }

    private val selectedApps: MutableLiveData<ArrayList<BatchPackageInfo>> by lazy {
        MutableLiveData<ArrayList<BatchPackageInfo>>().also {
            loadSelectedApps()
        }
    }

    fun getBatchData(): LiveData<ArrayList<BatchPackageInfo>> {
        return batchData
    }

    fun getSelectedApps(): LiveData<ArrayList<BatchPackageInfo>> {
        return selectedApps
    }

    @Suppress("UNCHECKED_CAST")
    private fun loadAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = (getInstalledApps() + getUninstalledApps()).toArrayList()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                apps.removeIf { it.packageName == applicationContext().packageName }
            }

            when (BatchPreferences.getAppsCategory()) {
                SortConstant.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                SortConstant.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            apps.getSortedList(BatchPreferences.getSortStyle(), BatchPreferences.isReverseSorting())

            val list = getBatchStateData(apps.clone() as ArrayList<PackageInfo>)
            var filtered = arrayListOf<BatchPackageInfo>()

            if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_SELECTED)) {
                filtered.addAll((list.clone() as ArrayList<BatchPackageInfo>).stream().filter {
                    it.isSelected
                }.collect(Collectors.toList()) as ArrayList<BatchPackageInfo>)
            }

            if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_NOT_SELECTED)) {
                filtered.addAll((list.clone() as ArrayList<BatchPackageInfo>).stream().filter {
                    !it.isSelected
                }.collect(Collectors.toList()) as ArrayList<BatchPackageInfo>)
            }

            if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_ENABLED)) {
                filtered.addAll((list.clone() as ArrayList<BatchPackageInfo>).stream().filter {
                    it.packageInfo.applicationInfo.enabled
                }.collect(Collectors.toList()) as ArrayList<BatchPackageInfo>)
            }

            if (FlagUtils.isFlagSet(BatchPreferences.getAppsFilter(), SortConstant.BATCH_DISABLED)) {
                filtered.addAll((list.clone() as ArrayList<BatchPackageInfo>).stream().filter {
                    !it.packageInfo.applicationInfo.enabled
                }.collect(Collectors.toList()) as ArrayList<BatchPackageInfo>)
            }

            filtered = filtered.stream().distinct().collect(Collectors.toList()) as ArrayList<BatchPackageInfo>

            batchData.postValue(filtered)
        }
    }

    private fun loadSelectedApps() {
        viewModelScope.launch(Dispatchers.IO) {
            selectedApps.postValue(getSelectedBatchStateData(getInstalledApps()))
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

    private fun getSelectedBatchStateData(apps: ArrayList<PackageInfo>): ArrayList<BatchPackageInfo> {
        batchDatabase = BatchDatabase.getInstance(context)

        var list = arrayListOf<BatchPackageInfo>()

        for (app in apps) {
            list.add(BatchPackageInfo(app, false, -1))
        }

        for (batch in batchDatabase!!.batchDao()!!.getSelectedApps()) {
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

        list = list.stream().filter { p -> p.isSelected }.collect(Collectors.toList()) as ArrayList<BatchPackageInfo>

        for (i in list.indices) {
            list[i].packageInfo.applicationInfo.name = PackageUtils.getApplicationName(
                    application.applicationContext, list[i].packageInfo.applicationInfo)
        }

        if (BatchPreferences.isSelectionOnTop()) {
            list.sortByDescending {
                it.isSelected
            }
        }

        return list
    }

    fun updateBatchItem(batchPackageInfo: BatchPackageInfo, update: Boolean = false) {
        viewModelScope.launch(Dispatchers.IO) {
            batchDatabase = BatchDatabase.getInstance(context)
            batchDatabase?.batchDao()
                ?.insertBatch(BatchModel(batchPackageInfo.packageInfo.packageName,
                                         batchPackageInfo.isSelected,
                                         System.currentTimeMillis()))

            if (update) {
                loadAppData()
            }
        }
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        loadAppData()
    }

    fun refresh() {
        loadAppData()
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        loadAppData()
    }

    override fun onCleared() {
        super.onCleared()
        batchDatabase?.close()
    }

    fun selectAllBatchItems() {
        viewModelScope.launch(Dispatchers.IO) {
            batchDatabase = BatchDatabase.getInstance(context)
            for (batchData in batchData.value!!) {
                if (!batchData.isSelected) {
                    batchDatabase?.batchDao()
                        ?.insertBatch(BatchModel(batchData.packageInfo.packageName,
                                                 true,
                                                 System.currentTimeMillis()))
                }
            }

            loadSelectedApps()
            loadAppData()
        }
    }

    fun deselectAllBatchItems() {
        viewModelScope.launch(Dispatchers.IO) {
            batchDatabase = BatchDatabase.getInstance(context)
            for (batchData in batchData.value!!) {
                if (batchData.isSelected) {
                    batchDatabase?.batchDao()
                        ?.insertBatch(BatchModel(batchData.packageInfo.packageName,
                                                 false,
                                                 System.currentTimeMillis()))
                }
            }

            loadSelectedApps()
            loadAppData()
        }
    }
}