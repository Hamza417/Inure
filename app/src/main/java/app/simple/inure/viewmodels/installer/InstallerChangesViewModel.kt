package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getPackageArchiveInfo
import app.simple.inure.apk.utils.PackageUtils.getPackageInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.Triple
import app.simple.inure.util.FileUtils.toFile
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
            packageInfo = packageManager.getPackageArchiveInfo(file)

            if (packageInfo == null) {
                postWarning("Failed to get package info")
                return@launch
            }

            oldPackageInfo = try {
                applicationContext().packageManager.getPackageInfo(packageInfo!!.packageName)
            } catch (e: Exception) {
                null
            }

            val list = arrayListOf<Triple<String, String, String>>()

            list.add(getPermissionChanges())
            list.add(getActivitiesChanges())
            list.add(getServicesChanges())
            list.add(getReceiversChanges())
            list.add(getProvidersChanges())
            // list.add(getNativeLibrariesChanges())
            list.add(getFeaturesChanges())

            changes.postValue(list)
        }
    }

    private fun getPermissionChanges(): Triple<String, String, String> {
        val title = getString(R.string.permissions)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val permissions = arrayListOf<String>()
        val oldPermissions = arrayListOf<String>()

        kotlin.runCatching {
            packageInfo!!.requestedPermissions?.let {
                permissions.addAll(it)
            }

            oldPackageInfo!!.requestedPermissions?.let {
                oldPermissions.addAll(it)
            }
        }

        added = buildString {
            for (permission in permissions) {
                if (!oldPermissions.contains(permission)) {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$permission\n")
                }
            }
        }

        removed = buildString {
            for (permission in oldPermissions) {
                if (!permissions.contains(permission)) {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$permission\n")
                }
            }
        }

        if (added.isEmpty()) {
            added = getString(R.string.no_new_additions)
        }

        if (removed.isEmpty()) {
            removed = getString(R.string.no_new_removals)
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getActivitiesChanges(): Triple<String, String, String> {
        val title = getString(R.string.activities)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val activities = arrayListOf<String>()
        val oldActivities = arrayListOf<String>()

        kotlin.runCatching {
            packageInfo!!.activities?.let {
                activities.addAll(it.map { activity -> activity.name })
            }

            oldPackageInfo!!.activities?.let {
                oldActivities.addAll(it.map { activity -> activity.name })
            }
        }

        added = buildString {
            for (activity in activities) {
                if (!oldActivities.contains(activity)) {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$activity\n")
                }
            }
        }

        removed = buildString {
            for (activity in oldActivities) {
                if (!activities.contains(activity)) {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$activity\n")
                }
            }
        }

        if (added.isEmpty()) {
            added = getString(R.string.no_new_additions)
        }

        if (removed.isEmpty()) {
            removed = getString(R.string.no_new_removals)
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getServicesChanges(): Triple<String, String, String> {
        val title = getString(R.string.services)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val services = arrayListOf<String>()
        val oldServices = arrayListOf<String>()

        kotlin.runCatching {
            packageInfo!!.services?.let {
                services.addAll(it.map { service -> service.name })
            }

            oldPackageInfo!!.services?.let {
                oldServices.addAll(it.map { service -> service.name })
            }
        }

        added = buildString {
            for (service in services) {
                if (!oldServices.contains(service)) {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$service\n")
                }
            }
        }

        removed = buildString {
            for (service in oldServices) {
                if (!services.contains(service)) {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$service\n")
                }
            }
        }

        if (added.isEmpty()) {
            added = getString(R.string.no_new_additions)
        }

        if (removed.isEmpty()) {
            removed = getString(R.string.no_new_removals)
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getReceiversChanges(): Triple<String, String, String> {
        val title = getString(R.string.receivers)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val receivers = arrayListOf<String>()
        val oldReceivers = arrayListOf<String>()

        kotlin.runCatching {
            packageInfo!!.receivers?.let {
                receivers.addAll(it.map { receiver -> receiver.name })
            }

            oldPackageInfo!!.receivers?.let {
                oldReceivers.addAll(it.map { receiver -> receiver.name })
            }
        }

        added = buildString {
            for (receiver in receivers) {
                if (!oldReceivers.contains(receiver)) {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$receiver\n")
                }
            }
        }

        removed = buildString {
            for (receiver in oldReceivers) {
                if (!receivers.contains(receiver)) {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$receiver\n")
                }
            }
        }

        if (added.isEmpty()) {
            added = getString(R.string.no_new_additions)
        }

        if (removed.isEmpty()) {
            removed = getString(R.string.no_new_removals)
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getProvidersChanges(): Triple<String, String, String> {
        val title = getString(R.string.providers)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val providers = arrayListOf<String>()
        val oldProviders = arrayListOf<String>()

        kotlin.runCatching {
            packageInfo!!.providers?.let {
                providers.addAll(it.map { provider -> provider.name })
            }

            oldPackageInfo!!.providers?.let {
                oldProviders.addAll(it.map { provider -> provider.name })
            }
        }

        added = buildString {
            for (provider in providers) {
                if (!oldProviders.contains(provider)) {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$provider\n")
                }
            }
        }

        removed = buildString {
            for (provider in oldProviders) {
                if (!providers.contains(provider)) {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$provider\n")
                }
            }
        }

        if (added.isEmpty()) {
            added = getString(R.string.no_new_additions)
        }

        if (removed.isEmpty()) {
            removed = getString(R.string.no_new_removals)
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getFeaturesChanges(): Triple<String, String, String> {
        val title = getString(R.string.uses_feature)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val features = arrayListOf<String>()
        val oldFeatures = arrayListOf<String>()

        kotlin.runCatching {
            packageInfo!!.reqFeatures?.let {
                features.addAll(it.map { feature -> feature.name })
            }

            oldPackageInfo!!.reqFeatures?.let {
                oldFeatures.addAll(it.map { feature -> feature.name })
            }
        }

        added = buildString {
            for (feature in features) {
                if (!oldFeatures.contains(feature)) {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$feature\n")
                }
            }
        }

        removed = buildString {
            for (feature in oldFeatures) {
                if (!features.contains(feature)) {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$feature\n")
                }
            }
        }

        if (added.isEmpty()) {
            added = getString(R.string.no_new_additions)
        }

        if (removed.isEmpty()) {
            removed = getString(R.string.no_new_removals)
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getNativeLibrariesChanges(): Triple<String, String, String> {
        val title = getString(R.string.native_libraries)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val nativeLibraries = arrayListOf<String>()
        val oldNativeLibraries = arrayListOf<String>()

        kotlin.runCatching {
            packageInfo!!.applicationInfo.nativeLibraryDir?.let {
                nativeLibraries.addAll(it.toFile().list().orEmpty())
            }

            oldPackageInfo!!.applicationInfo.nativeLibraryDir?.let {
                oldNativeLibraries.addAll(it.toFile().list().orEmpty())
            }
        }

        added = buildString {
            for (nativeLibrary in nativeLibraries) {
                if (!oldNativeLibraries.contains(nativeLibrary)) {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$nativeLibrary\n")
                }
            }
        }

        removed = buildString {
            for (nativeLibrary in oldNativeLibraries) {
                if (!nativeLibraries.contains(nativeLibrary)) {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$nativeLibrary\n")
                }
            }
        }

        if (added.isEmpty()) {
            added = getString(R.string.no_new_additions)
        }

        if (removed.isEmpty()) {
            removed = getString(R.string.no_new_removals)
        }

        return Triple(title, added.trim(), removed.trim())
    }
}