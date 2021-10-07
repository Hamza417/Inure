package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.FeatureInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.model.*
import com.jaredrummler.apkparser.model.AndroidComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.FileNotFoundException
import java.util.*

class ApkDataViewModel(application: Application, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val delay = 500L

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val activities: MutableLiveData<MutableList<ActivityInfoModel>> by lazy {
        MutableLiveData<MutableList<ActivityInfoModel>>().also {
            getActivitiesData()
        }
    }

    private val receivers: MutableLiveData<MutableList<ActivityInfoModel>> by lazy {
        MutableLiveData<MutableList<ActivityInfoModel>>().also {
            getReceiversData()
        }
    }

    private val extras: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getExtrasData()
        }
    }

    private val features: MutableLiveData<MutableList<FeatureInfo>> by lazy {
        MutableLiveData<MutableList<FeatureInfo>>().also {
            getFeaturesData()
        }
    }

    private val graphics: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getGraphicsData()
        }
    }

    private val permissions: MutableLiveData<MutableList<PermissionInfo>> by lazy {
        MutableLiveData<MutableList<PermissionInfo>>().also {
            loadPermissionData()
        }
    }

    private val providers: MutableLiveData<MutableList<ProviderInfoModel>> by lazy {
        MutableLiveData<MutableList<ProviderInfoModel>>().also {
            getProvidersData()
        }
    }

    private val resources: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getResourceData()
        }
    }

    private val services: MutableLiveData<MutableList<ServiceInfoModel>> by lazy {
        MutableLiveData<MutableList<ServiceInfoModel>>().also {
            getServicesData()
        }
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getActivities(): LiveData<MutableList<ActivityInfoModel>> {
        return activities
    }

    fun getReceivers(): LiveData<MutableList<ActivityInfoModel>> {
        return receivers
    }

    fun getExtras(): LiveData<MutableList<String>> {
        return extras
    }

    fun getFeatures(): LiveData<MutableList<FeatureInfo>> {
        return features
    }

    fun getGraphics(): LiveData<MutableList<String>> {
        return graphics
    }

    fun getPermissions(): LiveData<MutableList<PermissionInfo>> {
        return permissions
    }

    fun getProviders(): LiveData<MutableList<ProviderInfoModel>> {
        return providers
    }

    fun getResources(): LiveData<MutableList<String>> {
        return resources
    }

    fun getServices(): LiveData<MutableList<ServiceInfoModel>> {
        return services
    }

    private fun getActivitiesData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ActivityInfoModel>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_ACTIVITIES or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_ACTIVITIES or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (ai in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).activities) {
                    val activityInfoModel = ActivityInfoModel()

                    activityInfoModel.activityInfo = ai
                    activityInfoModel.name = ai.name
                    activityInfoModel.target = ai.targetActivity ?: getApplication<Application>().getString(R.string.not_available)
                    activityInfoModel.exported = ai.exported
                    activityInfoModel.permission = ai.permission ?: getApplication<Application>().getString(R.string.no_permission_required)

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getLaunchMode(ai.launchMode, getApplication()))
                        append(" | ")
                        append(MetaUtils.getOrientationString(ai.screenOrientation, getApplication()))

                        activityInfoModel.status = this.toString()
                    }

                    list.add(activityInfoModel)
                }

                activities.postValue(list)
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getReceiversData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ActivityInfoModel>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_RECEIVERS or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_RECEIVERS or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (ai in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).receivers) {
                    val activityInfoModel = ActivityInfoModel()

                    activityInfoModel.activityInfo = ai
                    activityInfoModel.name = ai.name
                    activityInfoModel.target = ai.targetActivity ?: getApplication<Application>().getString(R.string.not_available)
                    activityInfoModel.exported = ai.exported
                    activityInfoModel.permission = ai.permission ?: getApplication<Application>().getString(R.string.no_permission_required)

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getLaunchMode(ai.launchMode, getApplication()))
                        append(" | ")
                        append(MetaUtils.getOrientationString(ai.screenOrientation, getApplication()))
                        activityInfoModel.status = this.toString()
                    }

                    list.add(activityInfoModel)
                }

                receivers.postValue(list)
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getExtrasData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getExtraFiles(packageInfo.applicationInfo.sourceDir)) {
                    if (size == 0) {
                        throw FileNotFoundException("This package does not contain any extra files.")
                    } else {
                        extras.postValue(apply {
                            sortBy {
                                it.lowercase(Locale.getDefault())
                            }
                        })
                    }
                }
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getFeaturesData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<FeatureInfo>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_CONFIGURATIONS or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_CONFIGURATIONS or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (featureInfo in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).reqFeatures) {
                    list.add(featureInfo)
                }

                features.postValue(list)
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getGraphicsData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getGraphicsFiles(packageInfo.applicationInfo.sourceDir)) {
                    if (size == 0) {
                        throw FileNotFoundException("This package does not contain any graphical files.")
                    } else {
                        graphics.postValue(apply {
                            sortBy {
                                it.lowercase(Locale.getDefault())
                            }
                        })
                    }
                }
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun loadPermissionData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val appPackageInfo = getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, PackageManager.GET_PERMISSIONS)
                val permissions = arrayListOf<PermissionInfo>()

                for (permission in appPackageInfo.requestedPermissions) {
                    for (flags in appPackageInfo.requestedPermissionsFlags) {
                        if (flags and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                            permissions.add(PermissionInfo(true, permission))
                            break
                        } else {
                            permissions.add(PermissionInfo(false, permission))
                            break
                        }
                    }
                }

                this@ApkDataViewModel.permissions.postValue(permissions.apply {
                    sortBy {
                        it.name.lowercase(Locale.getDefault())
                    }
                })
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getProvidersData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ProviderInfoModel>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_PROVIDERS or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_PROVIDERS or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (pi in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).providers) {
                    val providerInfoModel = ProviderInfoModel()


                    providerInfoModel.providerInfo = pi
                    providerInfoModel.name = pi.name
                    providerInfoModel.authority = pi.authority
                    providerInfoModel.isExported = pi.exported
                    providerInfoModel.permissions = pi.readPermission + pi.writePermission

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getServiceFlags(pi.flags, getApplication()))

                        providerInfoModel.status = this.toString()
                    }

                    list.add(providerInfoModel)
                }

                providers.postValue(list)
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getResourceData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                with(APKParser.getXmlFiles(packageInfo.applicationInfo.sourceDir)) {
                    if (size == 0) {
                        throw FileNotFoundException("This package does not contain any xml resource files.")
                    } else {
                        resources.postValue(apply {
                            sortBy {
                                it.lowercase(Locale.getDefault())
                            }
                        })
                    }
                }
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getServicesData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ServiceInfoModel>()

                val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    PackageManager.GET_SERVICES or PackageManager.MATCH_DISABLED_COMPONENTS
                } else {
                    @Suppress("deprecation")
                    PackageManager.GET_SERVICES or PackageManager.GET_DISABLED_COMPONENTS
                }

                for (info in getApplication<Application>().packageManager.getPackageInfo(packageInfo.packageName, flags).services) {
                    val serviceInfoModel = ServiceInfoModel()

                    serviceInfoModel.serviceInfo = info
                    serviceInfoModel.name = info.name
                    serviceInfoModel.isExported = info.exported
                    serviceInfoModel.flags = info.flags
                    serviceInfoModel.name = info.name
                    serviceInfoModel.foregroundType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) info.foregroundServiceType else -3
                    serviceInfoModel.permissions = info.permission ?: getApplication<Application>().getString(R.string.no_permission_required)

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getForegroundServiceType(serviceInfoModel.foregroundType, getApplication()))
                        append(" | ")
                        append(MetaUtils.getServiceFlags(info.flags, getApplication()))

                        serviceInfoModel.status = this.toString()
                    }

                    list.add(serviceInfoModel)
                }

                services.postValue(list)
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    /**
     * For some reason this did not work.
     *
     * TODO - add explanation to why
     */
    @Deprecated("This won't work",
                ReplaceWith("this.apply { sortedBy { it.name.substring(it.name.lastIndexOf(\".\")) } }"))
    private fun MutableList<AndroidComponent>.sort(): MutableList<AndroidComponent> {
        return this.apply {
            sortedBy {
                it.name.substring(it.name.lastIndexOf("."))
            }
        }
    }
}
