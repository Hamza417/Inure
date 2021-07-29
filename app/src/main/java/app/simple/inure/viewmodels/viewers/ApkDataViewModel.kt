package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.parsers.APKParser
import app.simple.inure.apk.parsers.APKParser.getActivities
import app.simple.inure.apk.parsers.APKParser.getFeatures
import app.simple.inure.apk.parsers.APKParser.getPermissions
import app.simple.inure.apk.parsers.APKParser.getProviders
import app.simple.inure.apk.parsers.APKParser.getReceivers
import app.simple.inure.apk.parsers.APKParser.getServices
import app.simple.inure.model.PermissionInfo
import app.simple.inure.model.UsesFeatures
import com.jaredrummler.apkparser.model.AndroidComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

class ApkDataViewModel(application: Application, val param: ApplicationInfo) : AndroidViewModel(application) {

    private val delay = 500L

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val activities: MutableLiveData<MutableList<AndroidComponent>> by lazy {
        MutableLiveData<MutableList<AndroidComponent>>().also {
            getActivitiesData()
        }
    }

    private val receivers: MutableLiveData<MutableList<AndroidComponent>> by lazy {
        MutableLiveData<MutableList<AndroidComponent>>().also {
            getReceiversData()
        }
    }

    private val extras: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getExtrasData()
        }
    }

    private val features: MutableLiveData<MutableList<UsesFeatures>> by lazy {
        MutableLiveData<MutableList<UsesFeatures>>().also {
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

    private val providers: MutableLiveData<MutableList<AndroidComponent>> by lazy {
        MutableLiveData<MutableList<AndroidComponent>>().also {
            getProvidersData()
        }
    }

    private val resources: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getResourceData()
        }
    }

    private val services: MutableLiveData<MutableList<AndroidComponent>> by lazy {
        MutableLiveData<MutableList<AndroidComponent>>().also {
            getServicesData()
        }
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun getActivities(): LiveData<MutableList<AndroidComponent>> {
        return activities
    }

    fun getReceivers(): LiveData<MutableList<AndroidComponent>> {
        return receivers
    }

    fun getExtras(): LiveData<MutableList<String>> {
        return extras
    }

    fun getFeatures(): LiveData<MutableList<UsesFeatures>> {
        return features
    }

    fun getGraphics(): LiveData<MutableList<String>> {
        return graphics
    }

    fun getPermissions(): LiveData<MutableList<PermissionInfo>> {
        return permissions
    }

    fun getProviders(): LiveData<MutableList<AndroidComponent>> {
        return providers
    }

    fun getResources(): LiveData<MutableList<String>> {
        return resources
    }

    fun getServices(): LiveData<MutableList<AndroidComponent>> {
        return services
    }

    private fun getActivitiesData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                activities.postValue(param.getActivities()!!.apply {
                    sortBy {
                        it.name.substring(it.name.lastIndexOf("."))
                    }
                })
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getReceiversData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                receivers.postValue(param.getReceivers()!!.apply {
                    sortBy {
                        it.name.substring(it.name.lastIndexOf("."))
                    }
                })
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getExtrasData() {
        viewModelScope.launch(Dispatchers.Default) {
            extras.postValue(APKParser.getExtraFiles(param.sourceDir).apply {
                sortBy {
                    it.lowercase(Locale.getDefault())
                }
            })
        }
    }

    private fun getFeaturesData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                features.postValue(param.getFeatures().apply {
                    sortBy {
                        it.name
                    }
                })
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getGraphicsData() {
        viewModelScope.launch(Dispatchers.Default) {
            graphics.postValue(APKParser.getGraphicsFiles(param.sourceDir).apply {
                sortBy {
                    it.lowercase(Locale.getDefault())
                }
            })
        }
    }

    private fun loadPermissionData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val permissionsList = param.getPermissions()
                val packageInfo = getApplication<Application>().packageManager.getPackageInfo(param.packageName, PackageManager.GET_PERMISSIONS)
                val permissions = arrayListOf<PermissionInfo>()

                for (x in permissionsList.indices) {
                    for (y in packageInfo.requestedPermissions.indices) {
                        if (permissionsList[x] == packageInfo.requestedPermissions[y]) {
                            if (packageInfo.requestedPermissionsFlags[y] and PackageInfo.REQUESTED_PERMISSION_GRANTED != 0) {
                                permissions.add(PermissionInfo(true, permissionsList[x]))
                            } else {
                                permissions.add(PermissionInfo(false, permissionsList[x]))
                            }
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
                providers.postValue(param.getProviders()!!.apply {
                    sortBy {
                        it.name.substring(it.name.lastIndexOf("."))
                    }
                })
            }.getOrElse {
                delay(delay)
                error.postValue(it.message)
            }
        }
    }

    private fun getResourceData() {
        viewModelScope.launch(Dispatchers.Default) {
            resources.postValue(APKParser.getXmlFiles(param.sourceDir).apply {
                sortBy {
                    it.lowercase(Locale.getDefault())
                }
            })
        }
    }

    private fun getServicesData() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                services.postValue(param.getServices()!!.apply {
                    sortBy {
                        it.name.substring(it.name.lastIndexOf("."))
                    }
                })
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
