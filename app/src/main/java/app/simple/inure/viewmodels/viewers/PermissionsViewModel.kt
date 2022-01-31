package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PermissionUtils.getPermissionInfo
import app.simple.inure.constants.Misc
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.models.PermissionInfo
import app.simple.inure.util.StringUtils.capitalizeFirstLetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class PermissionsViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val permissions: MutableLiveData<MutableList<PermissionInfo>> by lazy {
        MutableLiveData<MutableList<PermissionInfo>>().also {
            loadPermissionData("")
        }
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    fun getPermissions(): LiveData<MutableList<PermissionInfo>> {
        return permissions
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun loadPermissionData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val context = context
                val appPackageInfo = getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_PERMISSIONS)
                val permissions = arrayListOf<PermissionInfo>()

                for (count in appPackageInfo.requestedPermissions.indices) {
                    val permissionInfo = PermissionInfo()

                    kotlin.runCatching {
                        permissionInfo.permissionInfo = appPackageInfo.requestedPermissions[count].getPermissionInfo(context)
                        permissionInfo.label = permissionInfo.permissionInfo!!.loadLabel(context.packageManager).toString().capitalizeFirstLetter()

                        if (isKeywordMatched(keyword, appPackageInfo.requestedPermissions[count], permissionInfo.label)) {
                            permissionInfo.isGranted = appPackageInfo.requestedPermissionsFlags[count] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0
                            permissionInfo.name = appPackageInfo.requestedPermissions[count]
                            permissions.add(permissionInfo)
                        }
                    }.onFailure {
                        permissionInfo.permissionInfo = null
                        permissionInfo.label = appPackageInfo.requestedPermissions[count]

                        if (isKeywordMatched(keyword, appPackageInfo.requestedPermissions[count])) {
                            permissionInfo.isGranted = appPackageInfo.requestedPermissionsFlags[count] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0
                            permissionInfo.name = appPackageInfo.requestedPermissions[count]
                            permissions.add(permissionInfo)
                        }

                        it.printStackTrace()
                    }
                }

                this@PermissionsViewModel.permissions.postValue(permissions.apply {
                    sortBy {
                        it.name.lowercase(Locale.getDefault())
                    }
                })
            }.getOrElse {
                delay(Misc.delay)
                error.postValue(it.stackTraceToString())
            }
        }
    }

    private fun isKeywordMatched(keyword: String, name: String, loadLabel: String): Boolean {
        return name.lowercase().contains(keyword.lowercase()) || loadLabel.lowercase().contains(keyword.lowercase())
    }

    private fun isKeywordMatched(keyword: String, name: String): Boolean {
        return name.lowercase().contains(keyword.lowercase())
    }
}