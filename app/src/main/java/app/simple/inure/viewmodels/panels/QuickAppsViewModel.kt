package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.database.instances.QuickAppsDatabase
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.models.QuickApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickAppsViewModel(application: Application) : WrappedViewModel(application) {

    private var db: QuickAppsDatabase? = null

    private val quickApps: MutableLiveData<MutableList<PackageInfo>> by lazy {
        MutableLiveData<MutableList<PackageInfo>>().also {
            loadQuickApps()
        }
    }

    private val simpleQuickAppList: MutableLiveData<MutableList<QuickApp>> by lazy {
        MutableLiveData<MutableList<QuickApp>>().also {
            loadSimpleQuickAppsData()
        }
    }

    fun getQuickApps(): LiveData<MutableList<PackageInfo>> {
        return quickApps
    }

    fun getSimpleQuickAppList(): LiveData<MutableList<QuickApp>> {
        return simpleQuickAppList
    }

    private fun loadQuickApps() {
        viewModelScope.launch(Dispatchers.IO) {
            db = QuickAppsDatabase.getInstance(context)

            val quickApps = db?.quickAppsDao()?.getAllQuickApps()!!
            val apps = arrayListOf<PackageInfo>()

            for (quickApp in quickApps) {
                apps.add(packageManager.getPackageInfo(quickApp.packageName, PackageManager.GET_META_DATA))
            }

            for (i in apps.indices) {
                apps[i].applicationInfo.name = PackageUtils.getApplicationName(getApplication<Application>()
                                                                                   .applicationContext, apps[i].applicationInfo)
            }

            this@QuickAppsViewModel.quickApps.postValue(apps)
        }
    }

    fun addQuickApp(packageId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            db = QuickAppsDatabase.getInstance(context)

            db?.quickAppsDao()?.insertQuickApp(QuickApp(System.currentTimeMillis(), packageId))
            loadQuickApps()
            loadSimpleQuickAppsData()
        }
    }

    fun removeQuickApp(packageId: String) {
        viewModelScope.launch(Dispatchers.Default) {
            db = QuickAppsDatabase.getInstance(context)

            db?.quickAppsDao()?.deleteQuickApp(QuickApp(System.currentTimeMillis(), packageId))
            loadQuickApps()
            loadSimpleQuickAppsData()
        }
    }

    private fun loadSimpleQuickAppsData() {
        viewModelScope.launch(Dispatchers.Default) {
            db = QuickAppsDatabase.getInstance(context)
            simpleQuickAppList.postValue(db?.quickAppsDao()?.getQuickApps())
        }
    }

    override fun onCleared() {
        super.onCleared()
        db?.close()
    }
}