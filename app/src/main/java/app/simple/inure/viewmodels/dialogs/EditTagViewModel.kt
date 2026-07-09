package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.Tag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditTagViewModel(application: Application, private val tag: Tag) : PackageUtilsViewModel(application) {

    private val loadedPackages: MutableLiveData<MutableList<Package>> by lazy {
        MutableLiveData<MutableList<Package>>()
    }

    fun getLoadedPackages(): LiveData<MutableList<Package>> {
        return loadedPackages
    }

    private fun loadPackages(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.Default) {
            val packages = tag.packagesAsList.map {
                val packageInfo = apps.find { app -> app.packageName == it }
                Package(packageName = it,
                        appName = packageInfo?.applicationInfo?.name
                            ?: packageInfo?.packageName
                            ?: it)
            }.toMutableList()

            loadedPackages.postValue(packages)
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        loadPackages(apps)
    }

    companion object {
        private const val TAG = "EditTagViewModel"

        data class Package(val packageName: String, val appName: String)
    }
}