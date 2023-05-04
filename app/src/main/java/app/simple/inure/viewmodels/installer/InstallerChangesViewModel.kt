package app.simple.inure.viewmodels.installer

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
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
                PackageManager.MATCH_DISABLED_COMPONENTS
    } else {
        PackageManager.GET_ACTIVITIES or
                PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_PERMISSIONS or
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
            packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                applicationContext().packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.PackageInfoFlags.of(flags.toLong()))!!
            } else {
                @Suppress("DEPRECATION")
                applicationContext().packageManager.getPackageArchiveInfo(file.absolutePath, flags)!!
            }

            if (packageInfo == null) {
                postWarning("Failed to get package info")
                return@launch
            }

            oldPackageInfo = try {
                applicationContext().packageManager.getPackageInfo(packageInfo!!.packageName)
            } catch (e: Exception) {
                postWarning("Failed to get package info")
                return@launch
            }

            val list = arrayListOf<Triple<String, String, String>>()

            list.add(getPermissionChanges())
            list.add(getActivitiesChanges())
            list.add(getServicesChanges())
            list.add(getReceiversChanges())
            list.add(getProvidersChanges())
            list.add(getNativeLibrariesChanges())
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

        packageInfo!!.requestedPermissions?.let {
            permissions.addAll(it)
        }

        oldPackageInfo!!.requestedPermissions?.let {
            oldPermissions.addAll(it)
        }

        for (permission in permissions) {
            if (!oldPermissions.contains(permission)) {
                added = buildString {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$permission\n")
                }
            }
        }

        for (permission in oldPermissions) {
            if (!permissions.contains(permission)) {
                removed = buildString {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$permission\n")
                }
            }
        }



        return Triple(title, added.trim(), removed.trim())
    }

    private fun getActivitiesChanges(): Triple<String, String, String> {
        val title = getString(R.string.activities)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val activities = arrayListOf<String>()
        val oldActivities = arrayListOf<String>()

        packageInfo!!.activities?.let {
            activities.addAll(it.map { activity -> activity.name })
        }

        oldPackageInfo!!.activities?.let {
            oldActivities.addAll(it.map { activity -> activity.name })
        }

        for (activity in activities) {
            if (!oldActivities.contains(activity)) {
                added = buildString {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$activity\n")
                }
            }
        }

        for (activity in oldActivities) {
            if (!activities.contains(activity)) {
                removed = buildString {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$activity\n")
                }
            }
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getServicesChanges(): Triple<String, String, String> {
        val title = getString(R.string.services)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val services = arrayListOf<String>()
        val oldServices = arrayListOf<String>()

        packageInfo!!.services?.let {
            services.addAll(it.map { service -> service.name })
        }

        oldPackageInfo!!.services?.let {
            oldServices.addAll(it.map { service -> service.name })
        }

        for (service in services) {
            if (!oldServices.contains(service)) {
                added = buildString {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$service\n")
                }
            }
        }

        for (service in oldServices) {
            if (!services.contains(service)) {
                removed = buildString {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$service\n")
                }
            }
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getReceiversChanges(): Triple<String, String, String> {
        val title = getString(R.string.receivers)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val receivers = arrayListOf<String>()
        val oldReceivers = arrayListOf<String>()

        packageInfo!!.receivers?.let {
            receivers.addAll(it.map { receiver -> receiver.name })
        }

        oldPackageInfo!!.receivers?.let {
            oldReceivers.addAll(it.map { receiver -> receiver.name })
        }

        for (receiver in receivers) {
            if (!oldReceivers.contains(receiver)) {
                added = buildString {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$receiver\n")
                }
            }
        }

        for (receiver in oldReceivers) {
            if (!receivers.contains(receiver)) {
                removed = buildString {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$receiver\n")
                }
            }
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getProvidersChanges(): Triple<String, String, String> {
        val title = getString(R.string.providers)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val providers = arrayListOf<String>()
        val oldProviders = arrayListOf<String>()

        packageInfo!!.providers?.let {
            providers.addAll(it.map { provider -> provider.name })
        }

        oldPackageInfo!!.providers?.let {
            oldProviders.addAll(it.map { provider -> provider.name })
        }

        for (provider in providers) {
            if (!oldProviders.contains(provider)) {
                added = buildString {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$provider\n")
                }
            }
        }

        for (provider in oldProviders) {
            if (!providers.contains(provider)) {
                removed = buildString {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$provider\n")
                }
            }
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getFeaturesChanges(): Triple<String, String, String> {
        val title = getString(R.string.uses_feature)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val features = arrayListOf<String>()
        val oldFeatures = arrayListOf<String>()

        packageInfo!!.reqFeatures?.let {
            features.addAll(it.map { feature -> feature.name })
        }

        oldPackageInfo!!.reqFeatures?.let {
            oldFeatures.addAll(it.map { feature -> feature.name })
        }

        for (feature in features) {
            if (!oldFeatures.contains(feature)) {
                added = buildString {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$feature\n")
                }
            }
        }

        for (feature in oldFeatures) {
            if (!features.contains(feature)) {
                removed = buildString {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$feature\n")
                }
            }
        }

        return Triple(title, added.trim(), removed.trim())
    }

    private fun getNativeLibrariesChanges(): Triple<String, String, String> {
        val title = getString(R.string.native_libraries)
        var added = getString(R.string.no_new_additions)
        var removed = getString(R.string.no_new_removals)

        val nativeLibraries = arrayListOf<String>()
        val oldNativeLibraries = arrayListOf<String>()

        packageInfo!!.applicationInfo.nativeLibraryDir?.let {
            nativeLibraries.addAll(it.toFile().list().orEmpty())
        }

        oldPackageInfo!!.applicationInfo.nativeLibraryDir?.let {
            oldNativeLibraries.addAll(it.toFile().list().orEmpty())
        }

        for (nativeLibrary in nativeLibraries) {
            if (!oldNativeLibraries.contains(nativeLibrary)) {
                added = buildString {
                    if (added == getString(R.string.no_new_additions)) {
                        added = ""
                    }

                    append("$nativeLibrary\n")
                }
            }
        }

        for (nativeLibrary in oldNativeLibraries) {
            if (!nativeLibraries.contains(nativeLibrary)) {
                removed = buildString {
                    if (removed == getString(R.string.no_new_removals)) {
                        removed = ""
                    }

                    append("$nativeLibrary\n")
                }
            }
        }

        return Triple(title, added.trim(), removed.trim())
    }
}