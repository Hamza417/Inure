package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PermissionUtils.getPermissionInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.PermissionInfo
import app.simple.inure.util.StringUtils.capitalizeFirstLetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.util.*

class InstallerPermissionViewModel(application: Application, val file: File?) : WrappedViewModel(application) {

    private val permissionsFile: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().also {
            loadPermissions()
        }
    }

    private val permissionsInfo: MutableLiveData<ArrayList<PermissionInfo>> by lazy {
        MutableLiveData<ArrayList<PermissionInfo>>().also {
            loadPermissionData()
        }
    }

    fun getPermissionsFile(): LiveData<ArrayList<String>> {
        return permissionsFile
    }

    fun getPermissionsInfo(): LiveData<ArrayList<PermissionInfo>> {
        return permissionsInfo
    }

    private fun loadPermissions() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                ApkFile(file).use { apkFile ->
                    if (packageManager.isPackageInstalled(apkFile.apkMeta.packageName)) return@launch

                    val permissions = apkFile.apkMeta.usesPermissions
                    for (p in apkFile.apkMeta.permissions) {
                        permissions.add(p.name)
                    }
                    permissions.sortBy {
                        it.lowercase()
                    }

                    this@InstallerPermissionViewModel.permissionsFile.postValue(permissions as ArrayList<String> /* = java.util.ArrayList<kotlin.String> */)
                }
            }.getOrElse {
                it.printStackTrace()
            }
        }
    }

    private fun loadPermissionData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val context = context
                val apkFile = ApkFile(file)
                val appPackageInfo = packageManager.getPackageInfo(apkFile.apkMeta.packageName)!!
                val permissions = arrayListOf<PermissionInfo>()

                for (count in appPackageInfo.requestedPermissions.indices) {
                    val permissionInfo = PermissionInfo()

                    kotlin.runCatching {
                        permissionInfo.permissionInfo = appPackageInfo.requestedPermissions[count].getPermissionInfo(context)
                        permissionInfo.label = permissionInfo.permissionInfo!!.loadLabel(context.packageManager).toString().capitalizeFirstLetter()
                        if (appPackageInfo.requestedPermissionsFlags[count] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                            permissionInfo.isGranted = 1
                        } else {
                            permissionInfo.isGranted = 0
                        }
                        permissionInfo.name = appPackageInfo.requestedPermissions[count]
                        permissions.add(permissionInfo)
                    }.onFailure {
                        permissionInfo.permissionInfo = null
                        permissionInfo.label = appPackageInfo.requestedPermissions[count]
                        if (appPackageInfo.requestedPermissionsFlags[count] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                            permissionInfo.isGranted = 1
                        } else {
                            permissionInfo.isGranted = 0
                        }
                        permissionInfo.name = appPackageInfo.requestedPermissions[count]
                        permissions.add(permissionInfo)

                        it.printStackTrace()
                    }
                }
                val requestedPermissions = appPackageInfo.requestedPermissions.toMutableList()

                apkFile.apkMeta.permissions.forEach { permission ->
                    if (permission.name !in requestedPermissions) {
                        val permissionInfo = PermissionInfo()

                        permissionInfo.permissionInfo = permission.name.getPermissionInfo(context)
                        permissionInfo.label = kotlin.runCatching {
                            permissionInfo.permissionInfo!!.loadLabel(context.packageManager).toString().capitalizeFirstLetter()
                        }.getOrElse {
                            permission.name
                        }

                        permissionInfo.isGranted = 2
                        permissionInfo.name = permission.name
                        permissions.add(permissionInfo)
                    }
                }

                apkFile.close()

                permissionsInfo.postValue(permissions.apply {
                    sortBy {
                        it.name.lowercase(Locale.getDefault())
                    }
                })
            }.getOrElse {
                postError(it)
            }
        }
    }
}