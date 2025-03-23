package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.Triple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class InstallerChangesViewModel(application: Application, val file: File) : WrappedViewModel(application) {

    private var packageInfo: PackageInfo? = null
    private var oldPackageInfo: PackageInfo? = null

    @Suppress("DEPRECATION")
    private val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_META_DATA or
                PackageManager.MATCH_DISABLED_COMPONENTS
    } else {
        PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_PERMISSIONS or
                PackageManager.GET_META_DATA or
                PackageManager.GET_DISABLED_COMPONENTS
    }

    private val changes: MutableLiveData<ArrayList<Triple<String, String, String>>> by lazy {
        MutableLiveData<ArrayList<Triple<String, String, String>>>().also {
            loadChangesData()
        }
    }

    fun getChangesData(): MutableLiveData<ArrayList<Triple<String, String, String>>> {
        return changes
    }

    private fun loadChangesData() {
        viewModelScope.launch(Dispatchers.IO) {
            packageInfo = packageManager.getPackageArchiveInfo(file.absolutePath, flags)

            if (packageInfo == null) {
                postWarning("Failed to get package info")
                return@launch
            }

            oldPackageInfo = try {
                applicationContext().packageManager.getPackageInfo(packageInfo!!.packageName, flags)
            } catch (e: NameNotFoundException) {
                Log.e(TAG, "Failed to get installed package info", e)
                PackageInfo()
            }

            val list = arrayListOf<Triple<String, String, String>>()

            list.add(getPermissionChanges())
            list.add(getActivitiesChanges())
            list.add(getServicesChanges())
            list.add(getReceiversChanges())
            list.add(getProvidersChanges())
            list.add(getFeaturesChanges())

            changes.postValue(list)
        }
    }

    private fun getPermissionChanges(): Triple<String, String, String> {
        val title = getString(R.string.permissions)

        val permissions = packageInfo?.requestedPermissions?.toSet() ?: emptySet()
        val oldPermissions = oldPackageInfo?.requestedPermissions?.toSet() ?: emptySet()

        val added = permissions.subtract(oldPermissions).joinToString("\n").ifEmpty { getString(R.string.no_new_additions) }
        val removed = oldPermissions.subtract(permissions).joinToString("\n").ifEmpty { getString(R.string.no_new_removals) }

        return Triple(title, added, removed)
    }

    private fun getActivitiesChanges(): Triple<String, String, String> {
        val title = getString(R.string.activities)

        val activities = packageInfo?.activities?.map { it.name }?.toSet() ?: emptySet()
        val oldActivities = oldPackageInfo?.activities?.map { it.name }?.toSet() ?: emptySet()

        val added = activities.subtract(oldActivities).joinToString("\n").ifEmpty { getString(R.string.no_new_additions) }
        val removed = oldActivities.subtract(activities).joinToString("\n").ifEmpty { getString(R.string.no_new_removals) }

        return Triple(title, added, removed)
    }

    private fun getServicesChanges(): Triple<String, String, String> {
        val title = getString(R.string.services)

        val services = packageInfo?.services?.map { it.name }?.toSet() ?: emptySet()
        val oldServices = oldPackageInfo?.services?.map { it.name }?.toSet() ?: emptySet()

        val added = services.subtract(oldServices).joinToString("\n").ifEmpty { getString(R.string.no_new_additions) }
        val removed = oldServices.subtract(services).joinToString("\n").ifEmpty { getString(R.string.no_new_removals) }

        return Triple(title, added, removed)
    }

    private fun getReceiversChanges(): Triple<String, String, String> {
        val title = getString(R.string.receivers)

        val receivers = packageInfo?.receivers?.map { it.name }?.toSet() ?: emptySet()
        val oldReceivers = oldPackageInfo?.receivers?.map { it.name }?.toSet() ?: emptySet()

        val added = receivers.subtract(oldReceivers).joinToString("\n").ifEmpty { getString(R.string.no_new_additions) }
        val removed = oldReceivers.subtract(receivers).joinToString("\n").ifEmpty { getString(R.string.no_new_removals) }

        return Triple(title, added, removed)
    }

    private fun getProvidersChanges(): Triple<String, String, String> {
        val title = getString(R.string.providers)

        val providers = packageInfo?.providers?.map { it.name }?.toSet() ?: emptySet()
        val oldProviders = oldPackageInfo?.providers?.map { it.name }?.toSet() ?: emptySet()

        val added = providers.subtract(oldProviders).joinToString("\n").ifEmpty { getString(R.string.no_new_additions) }
        val removed = oldProviders.subtract(providers).joinToString("\n").ifEmpty { getString(R.string.no_new_removals) }

        return Triple(title, added, removed)
    }

    private fun getFeaturesChanges(): Triple<String, String, String> {
        val title = getString(R.string.uses_feature)

        val features = packageInfo?.reqFeatures?.map { it.name }?.toSet() ?: emptySet()
        val oldFeatures = oldPackageInfo?.reqFeatures?.map { it.name }?.toSet() ?: emptySet()

        val added = features.subtract(oldFeatures).joinToString("\n").ifEmpty { getString(R.string.no_new_additions) }
        val removed = oldFeatures.subtract(features).joinToString("\n").ifEmpty { getString(R.string.no_new_removals) }

        return Triple(title, added, removed)
    }

    companion object {
        private const val TAG = "InstallerChangesViewModel"
    }
}
