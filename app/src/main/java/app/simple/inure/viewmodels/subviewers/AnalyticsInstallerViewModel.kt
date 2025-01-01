package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getInstallerPackageName
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AnalyticsInstallerViewModel(application: Application, private val entry: Entry) : PackageUtilsViewModel(application) {

    private val apps1: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    fun getInstallerApps(): MutableLiveData<ArrayList<PackageInfo>> {
        return apps1
    }

    private fun loadInstallerApps(apps: ArrayList<PackageInfo>) {
        viewModelScope.launch(Dispatchers.Default) {
            val installerApps = arrayListOf<PackageInfo>()
            val unknown = getString(R.string.unknown)

            for (app in apps) {
                val installer = app.getInstallerPackageName(applicationContext()) ?: unknown
                if (installer.lowercase() == (entry as PieEntry).label.lowercase()) {
                    installerApps.add(app)
                }
            }

            this@AnalyticsInstallerViewModel.apps1.postValue(installerApps)
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        loadInstallerApps(apps)
    }
}
