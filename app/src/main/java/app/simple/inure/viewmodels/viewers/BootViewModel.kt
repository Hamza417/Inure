package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootViewModel(application: Application, private val packageInfo: PackageInfo) : RootShizukuViewModel(application) {

    private var keyword = ""
        set(value) {
            field = value
            loadBootData(value)
        }

    private var list: ArrayList<ResolveInfo>? = null
        get() {
            if (field == null) {
                field = getBootDataList()
            }

            return field
        }

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

    private val bootDataList: MutableLiveData<ArrayList<ResolveInfo>> by lazy {
        MutableLiveData<ArrayList<ResolveInfo>>().also {
            loadBootData(keyword)
        }
    }

    private val boot: MutableLiveData<ResolveInfo> by lazy {
        MutableLiveData<ResolveInfo>()
    }

    fun getBootData(): MutableLiveData<ArrayList<ResolveInfo>> {
        return bootDataList
    }

    fun getBootUpdate(): MutableLiveData<ResolveInfo> {
        return boot
    }

    private fun loadBootData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val arrayList = arrayListOf<ResolveInfo>()

            for (resolveInfo in list!!) {
                if (resolveInfo.activityInfo.name.lowercase().contains(keyword.lowercase()) || resolveInfo.activityInfo.packageName.lowercase().contains(keyword.lowercase())) {
                    arrayList.add(resolveInfo)
                }
            }

            bootDataList.postValue(arrayList)
        }
    }

    private fun getBootDataList(): ArrayList<ResolveInfo> {
        list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), PackageManager.ResolveInfoFlags.of(resolveInfoFlags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), resolveInfoFlags)
            }
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryBroadcastReceivers(PackageUtils.getIntentFilter(bootCompletedIntent), resolveInfoFlags)
        }.filter {
            it.activityInfo.packageName == packageInfo.packageName
        } as ArrayList<ResolveInfo>

        @Suppress("UNCHECKED_CAST")
        return (list as ArrayList<ResolveInfo>).clone() as ArrayList<ResolveInfo>
    }

    fun filterKeywords(keyword: String) {
        this.keyword = keyword
    }

    private fun disableComponent(resolveInfo: ResolveInfo) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                Shell.cmd("pm disable ${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}").exec().let {
                    if (it.isSuccess) {
                        boot.postValue(resolveInfo)
                    } else {
                        boot.postValue(null)
                        postWarning("Failed to disable component")
                    }
                }
            }
        }
    }

    private fun enableComponent(resolveInfo: ResolveInfo) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                Shell.cmd("pm enable ${resolveInfo.activityInfo.packageName}/${resolveInfo.activityInfo.name}").exec().let {
                    if (it.isSuccess) {
                        boot.postValue(resolveInfo)
                    } else {
                        boot.postValue(null)
                        postWarning("Failed to enable component")
                    }
                }
            }
        }
    }

    fun clear() {
        boot.postValue(null)
    }

    fun setComponentEnabled(resolveInfo: ResolveInfo, checked: Boolean) {
        if (checked) {
            disableComponent(resolveInfo)
        } else {
            enableComponent(resolveInfo)
        }
    }
}