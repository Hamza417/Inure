package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.util.APKParser
import app.simple.inure.util.APKParser.getActivities
import app.simple.inure.util.APKParser.getBroadcasts
import app.simple.inure.util.APKParser.getFeatures
import app.simple.inure.util.APKParser.getPermissions
import app.simple.inure.util.APKParser.getProviders
import app.simple.inure.util.APKParser.getServices
import com.jaredrummler.apkparser.model.AndroidComponent
import com.jaredrummler.apkparser.model.UseFeature
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ApkDataViewModel(application: Application, val param: ApplicationInfo) : AndroidViewModel(application) {

    private val activities: MutableLiveData<MutableList<AndroidComponent>> by lazy {
        MutableLiveData<MutableList<AndroidComponent>>().also {
            getActivitiesData()
        }
    }

    private val broadcasts: MutableLiveData<MutableList<AndroidComponent>> by lazy {
        MutableLiveData<MutableList<AndroidComponent>>().also {
            getBroadcastsData()
        }
    }

    private val extras: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getExtrasData()
        }
    }

    private val features: MutableLiveData<MutableList<UseFeature>> by lazy {
        MutableLiveData<MutableList<UseFeature>>().also {
            getFeaturesData()
        }
    }

    private val graphics: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getGraphicsData()
        }
    }

    private val permissions: MutableLiveData<MutableList<String>> by lazy {
        MutableLiveData<MutableList<String>>().also {
            getPermissionData()
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

    fun getActivities(): LiveData<MutableList<AndroidComponent>> {
        return activities
    }

    fun getBroadcasts(): LiveData<MutableList<AndroidComponent>> {
        return broadcasts
    }

    fun getExtras(): LiveData<MutableList<String>> {
        return extras
    }

    fun getFeatures(): LiveData<MutableList<UseFeature>> {
        return features
    }

    fun getGraphics(): LiveData<MutableList<String>> {
        return graphics
    }

    fun getPermissions(): LiveData<MutableList<String>> {
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
            activities.postValue(param.getActivities()!!.sort())
        }
    }

    private fun getBroadcastsData() {
        viewModelScope.launch(Dispatchers.Default) {
            broadcasts.postValue(param.getBroadcasts()!!.sort())
        }
    }

    private fun getExtrasData() {
        viewModelScope.launch(Dispatchers.Default) {
            extras.postValue(APKParser.getExtraFiles(param.sourceDir))
        }
    }

    private fun getFeaturesData() {
        viewModelScope.launch(Dispatchers.Default) {
            features.postValue(param.getFeatures().apply {
                this!!.sortBy {
                    it.name
                }
            })
        }
    }

    private fun getGraphicsData() {
        viewModelScope.launch(Dispatchers.Default) {
            graphics.postValue(APKParser.getGraphicsFiles(param.sourceDir))
        }
    }

    private fun getPermissionData() {
        viewModelScope.launch(Dispatchers.Default) {
            permissions.postValue(param.getPermissions())
        }
    }

    private fun getProvidersData() {
        viewModelScope.launch(Dispatchers.Default) {
            activities.postValue(param.getProviders()!!.sort())
        }
    }

    private fun getResourceData() {
        viewModelScope.launch(Dispatchers.Default) {
            resources.postValue(APKParser.getXmlFiles(param.sourceDir))
        }
    }

    private fun getServicesData() {
        viewModelScope.launch(Dispatchers.Default) {
            services.postValue(param.getServices()!!.sort())
        }
    }

    private fun MutableList<AndroidComponent>.sort(): MutableList<AndroidComponent> {
        this.sortedBy {
            it.name.substring(it.name.lastIndexOf(".") + 1)
        }

        return this
    }
}