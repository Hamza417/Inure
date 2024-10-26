package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.database.instances.QuickAppsDatabase
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.QuickApp
import app.simple.inure.services.DataLoaderService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickAppsViewModel(application: Application) : WrappedViewModel(application) {

    private var db: QuickAppsDatabase? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private var intentFilter: IntentFilter? = null

    init {
        intentFilter = IntentFilter()
        intentFilter?.addAction(DataLoaderService.RELOAD_QUICK_APPS)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == DataLoaderService.RELOAD_QUICK_APPS) {
                    loadQuickApps()
                    loadSimpleQuickAppsData()
                }
            }
        }

        LocalBroadcastManager.getInstance(application.applicationContext)
            .registerReceiver(broadcastReceiver!!, intentFilter!!)
    }

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
                packageManager.getPackageInfo(quickApp.packageName)?.let { apps.add(it) }
            }

            for (i in apps.indices) {
                apps[i].safeApplicationInfo.name = PackageUtils
                    .getApplicationName(application.applicationContext, apps[i].safeApplicationInfo)
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

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        loadQuickApps()
    }

    override fun onCleared() {
        super.onCleared()
        db?.close()
        LocalBroadcastManager.getInstance(application.applicationContext)
            .unregisterReceiver(broadcastReceiver!!)
    }
}
