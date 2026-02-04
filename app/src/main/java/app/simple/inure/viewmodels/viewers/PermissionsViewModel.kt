package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.Log
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.apk.utils.PermissionUtils.getPermissionInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.PermissionInfo
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.StringUtils.capitalizeFirstLetter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class PermissionsViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val _permissions = MutableStateFlow<MutableList<PermissionInfo>>(mutableListOf())
    val permissions: StateFlow<MutableList<PermissionInfo>> = _permissions.asStateFlow()

    // Track the last requested permission change
    private val _lastPermissionChangeRequest = MutableStateFlow<PermissionChangeRequest?>(null)
    val lastPermissionChangeRequest: StateFlow<PermissionChangeRequest?> = _lastPermissionChangeRequest.asStateFlow()

    // Track permission change results
    private val _permissionChangeResult = MutableStateFlow<PermissionChangeResult?>(null)
    val permissionChangeResult: StateFlow<PermissionChangeResult?> = _permissionChangeResult.asStateFlow()

    // Single permission update event
    private val _singlePermissionUpdate = MutableSharedFlow<PermissionUpdate>()
    val singlePermissionUpdate: SharedFlow<PermissionUpdate> = _singlePermissionUpdate.asSharedFlow()

    init {
        if (SearchPreferences.isSearchKeywordModeEnabled()) {
            Log.d("PermissionsViewModel", "Loading permission data with keyword: ${SearchPreferences.getLastSearchKeyword()}")
            loadPermissionData(SearchPreferences.getLastSearchKeyword())
        } else {
            loadPermissionData("")
        }
    }

    data class PermissionChangeRequest(
            val permissionName: String,
            val position: Int,
            val expectedStatus: Int, // 0 = revoked, 1 = granted
            val timestamp: Long = System.currentTimeMillis()
    )

    data class PermissionChangeResult(
            val permissionName: String,
            val position: Int,
            val success: Boolean,
            val actualStatus: Int
    )

    data class PermissionUpdate(
            val position: Int,
            val newStatus: Int
    )

    fun loadPermissionData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val context = context
                val appPackageInfo = if (packageManager.isPackageInstalled(packageInfo.packageName)) {
                    packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_PERMISSIONS)!!
                } else {
                    packageManager.getPackageArchiveInfo(packageInfo.safeApplicationInfo.sourceDir, PackageManager.GET_PERMISSIONS)!!
                }

                val permissions = arrayListOf<PermissionInfo>()

                for (count in appPackageInfo.requestedPermissions?.indices!!) {
                    val permissionInfo = PermissionInfo()

                    kotlin.runCatching {
                        permissionInfo.permissionInfo = appPackageInfo.requestedPermissions!![count].getPermissionInfo(context)
                        permissionInfo.label = permissionInfo.permissionInfo!!.loadLabel(context.packageManager).toString().capitalizeFirstLetter()

                        if (isKeywordMatched(keyword, appPackageInfo.requestedPermissions!![count], permissionInfo.label)) {
                            if (appPackageInfo.requestedPermissionsFlags!![count] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                                permissionInfo.isGranted = 1
                            } else {
                                permissionInfo.isGranted = 0
                            }
                            permissionInfo.name = appPackageInfo.requestedPermissions!![count]
                            permissions.add(permissionInfo)
                        }
                    }.onFailure {
                        permissionInfo.permissionInfo = null
                        permissionInfo.label = appPackageInfo.requestedPermissions!![count]

                        if (isKeywordMatched(keyword, appPackageInfo.requestedPermissions!![count])) {
                            if (appPackageInfo.requestedPermissionsFlags!![count] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                                permissionInfo.isGranted = 1
                            } else {
                                permissionInfo.isGranted = 0
                            }
                            permissionInfo.name = appPackageInfo.requestedPermissions!![count]
                            permissions.add(permissionInfo)
                        }
                    }
                }

                appPackageInfo.permissions

                /*
                val requestedPermissions = appPackageInfo.requestedPermissions.toMutableList()

                try {
                    ApkFile(packageInfo.safeApplicationInfo.sourceDir).use { apkFile ->
                        try {
                            apkFile.apkMeta.permissions.forEach { permission ->
                                if (permission.name !in requestedPermissions) {
                                    val permissionInfo = PermissionInfo()

                                    permissionInfo.permissionInfo = permission.name.getPermissionInfo(context)
                                    permissionInfo.label = kotlin.runCatching {
                                        permissionInfo.permissionInfo!!.loadLabel(context.packageManager).toString().capitalizeFirstLetter()
                                    }.getOrElse {
                                        permission.name
                                    }

                                    if (isKeywordMatched(keyword, permission.name, permissionInfo.label)) {
                                        permissionInfo.isGranted = 2
                                        permissionInfo.name = permission.name
                                        permissions.add(permissionInfo)
                                    }
                                }
                            }
                        } catch (e: ParserException) {
                            e.printStackTrace()
                        }
                    }
                } catch (e: java.lang.ClassCastException) {
                    e.printStackTrace()
                }
                */

                _permissions.value = permissions.apply {
                    sortBy {
                        it.name.lowercase(Locale.getDefault())
                    }
                }
            }.getOrElse {
                if (it is java.lang.NullPointerException) {
                    postWarning(getString(R.string.this_app_doesnt_require_any_permissions))
                } else {
                    postError(it)
                }
            }
        }
    }

    /**
     * Record a permission change request before attempting to change it
     */
    fun recordPermissionChangeRequest(permissionName: String, position: Int, expectedStatus: Int) {
        _lastPermissionChangeRequest.value = PermissionChangeRequest(
                permissionName = permissionName,
                position = position,
                expectedStatus = expectedStatus
        )
    }

    /**
     * Refresh a single permission's status and verify if the change was successful
     */
    fun refreshPermissionStatus(permissionName: String, position: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                if (packageManager.isPackageInstalled(packageInfo.packageName)) {
                    val appPackageInfo = packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_PERMISSIONS)!!

                    val permissionIndex = appPackageInfo.requestedPermissions?.indexOf(permissionName) ?: -1

                    if (permissionIndex != -1) {
                        val actualStatus = if (appPackageInfo.requestedPermissionsFlags!![permissionIndex] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                            1 // Granted
                        } else {
                            0 // Revoked
                        }

                        // Update the permission in the internal list
                        val currentPermissions = _permissions.value
                        if (position >= 0 && position < currentPermissions.size) {
                            currentPermissions[position].isGranted = actualStatus
                            // Emit single permission update event instead of recreating entire list
                            _singlePermissionUpdate.emit(PermissionUpdate(position, actualStatus))
                        }

                        // Check if the change was successful
                        val lastRequest = _lastPermissionChangeRequest.value
                        if (lastRequest != null && lastRequest.permissionName == permissionName) {
                            val success = actualStatus == lastRequest.expectedStatus
                            _permissionChangeResult.value = PermissionChangeResult(
                                    permissionName = permissionName,
                                    position = position,
                                    success = success,
                                    actualStatus = actualStatus
                            )
                        }
                    }
                }
            }.getOrElse {
                Log.e("PermissionsViewModel", "Failed to refresh permission status", it)
            }
        }
    }

    /**
     * Clear the last permission change result
     */
    fun clearPermissionChangeResult() {
        _permissionChangeResult.value = null
    }

    private fun isKeywordMatched(keyword: String, name: String, loadLabel: String): Boolean {
        return name.lowercase().contains(keyword.lowercase()) || loadLabel.lowercase().contains(keyword.lowercase())
    }

    private fun isKeywordMatched(keyword: String, name: String): Boolean {
        return name.lowercase().contains(keyword.lowercase())
    }
}
