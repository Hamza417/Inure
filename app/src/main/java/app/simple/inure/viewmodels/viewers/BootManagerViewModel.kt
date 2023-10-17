package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.ReceiversUtils
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import app.simple.inure.models.ActivityInfoModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootManagerViewModel(application: Application, private val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private val command = "pm query-receivers --components -a android.intent.action.BOOT_COMPLETED"
    private val bootCompletedIntent = "android.intent.action.BOOT_COMPLETED"

    private val resolveInfoFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        PackageManager.MATCH_DIRECT_BOOT_AWARE or PackageManager.MATCH_DIRECT_BOOT_UNAWARE or
                PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_DISABLED_UNTIL_USED_COMPONENTS or
                PackageManager.GET_RECEIVERS
    } else {
        @Suppress("DEPRECATION")
        PackageManager.GET_RECEIVERS or PackageManager.GET_DISABLED_COMPONENTS or
                PackageManager.GET_DISABLED_UNTIL_USED_COMPONENTS
    }

    private val bootComponentData: MutableLiveData<ArrayList<ActivityInfoModel>> by lazy {
        MutableLiveData<ArrayList<ActivityInfoModel>>()
    }

    private val bootManagerModelData: MutableLiveData<Pair<ActivityInfoModel, Int>> by lazy {
        MutableLiveData<Pair<ActivityInfoModel, Int>>()
    }

    fun getBootComponentData(): LiveData<ArrayList<ActivityInfoModel>> {
        return bootComponentData
    }

    fun getActivityInfoModelData(): LiveData<Pair<ActivityInfoModel, Int>> {
        return bootManagerModelData
    }

    init {
        initializeCoreFramework()
    }

    private fun loadBootComponents() {
        viewModelScope.launch(Dispatchers.IO) {
            val list: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), PackageManager.ResolveInfoFlags.of(resolveInfoFlags.toLong()))
                } else {
                    packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), resolveInfoFlags)
                }
            } else {
                packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), resolveInfoFlags)
            }.filter { it.activityInfo.packageName.equals(packageInfo.packageName) }

            val activityInfoModelArrayList = ArrayList<ActivityInfoModel>()

            list.forEach { resolveInfo ->
                val activityInfoModel = ActivityInfoModel()
                activityInfoModel.activityInfo = resolveInfo.activityInfo
                activityInfoModel.isReceiver = true
                activityInfoModel.name = resolveInfo.activityInfo.name
                activityInfoModel.isEnabled = ReceiversUtils.isEnabled(applicationContext(), packageInfo.packageName, resolveInfo.activityInfo.name)
                activityInfoModelArrayList.add(activityInfoModel)
            }

            activityInfoModelArrayList.sortBy {
                it.name.substringAfterLast(".")
            }

            bootComponentData.postValue(activityInfoModelArrayList)
        }
    }

    override fun onShellCreated(shell: Shell?) {

    }

    override fun onShellDenied() {

    }

    override fun onShizukuCreated() {

    }

}