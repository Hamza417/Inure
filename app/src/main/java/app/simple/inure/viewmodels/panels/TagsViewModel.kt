package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.parsers.FOSSParser
import app.simple.inure.apk.utils.PackageUtils.isXposedModule
import app.simple.inure.database.instances.TagsDatabase
import app.simple.inure.dialogs.tags.AutoTag
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.BatchPackageInfo
import app.simple.inure.models.Tag
import app.simple.inure.models.Tracker
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.TrackerUtils
import app.simple.inure.util.TrackerUtils.getComponentsPackageInfo
import app.simple.inure.util.TrackerUtils.hasTrackers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TagsViewModel(application: Application) : PackageUtilsViewModel(application) {

    private val tags: MutableLiveData<ArrayList<Tag>> by lazy {
        MutableLiveData<ArrayList<Tag>>()
    }

    private val tagNames: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>()
    }

    fun getTags(): LiveData<ArrayList<Tag>> {
        return tags
    }

    fun getTagNames(): LiveData<ArrayList<String>> {
        return tagNames
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        refresh()
    }

    private suspend fun loadTags() {
        val database = TagsDatabase.getInstance(application.applicationContext)
        val tags = database?.getTagDao()?.getTags()
        val apps = getInstalledApps() + getUninstalledApps()

        /**
         * Filter all uninstalled apps from [Tag.packages]
         */
        tags?.forEach { tag ->
            tag.packages = tag.packages.split(",").filter { packageName ->
                apps.any { app ->
                    app.packageName == packageName
                }
            }.joinToString(",")
        }

        /**
         * Make sure at least one app is installed from the [Tag.packages]
         * This is to prevent empty tags from showing up
         */
        val filtered = tags?.toArrayList()?.filter {
            it.packages.isNotEmpty() && it.packages.split(",").any { packageName ->
                apps.any { app ->
                    app.packageName == packageName
                }
            }
        }

        this@TagsViewModel.tags.postValue(filtered?.toArrayList())
    }

    private suspend fun loadTagNames() {
        val database = TagsDatabase.getInstance(application.applicationContext)
        val tags = database?.getTagDao()?.getTags()
        val apps = getInstalledApps() + getUninstalledApps()

        val filtered = tags?.toArrayList()?.filter {
            it.packages.isNotEmpty() && it.packages.split(",").any { packageName ->
                apps.any { app ->
                    app.packageName == packageName
                }
            }
        }?.map {
            it.tag
        }?.toArrayList()

        tagNames.postValue(filtered)
    }

    fun addTag(tag: String, packageInfo: PackageInfo, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsNameOnly()

            if (tags.isNullOrEmpty().invert()) {
                if (tags!!.contains(tag)) {
                    database.getTagDao()!!.getTag(tag).apply {
                        packages = packages.split(",")
                            .plus(packageInfo.packageName)
                            .distinct()
                            .joinToString(",")

                        database.getTagDao()!!.updateTag(this)
                    }
                } else {
                    database.getTagDao()!!.insertTag(Tag(tag, packageInfo.packageName, -1))
                }
            } else {
                database?.getTagDao()!!.insertTag(Tag(tag, packageInfo.packageName, -1))
            }

            withContext(Dispatchers.Main) {
                function()
                refresh()
            }
        }
    }

    fun removeTag(tag: String, packageInfo: PackageInfo, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsByPackage(packageInfo.packageName)?.toSet()

            if (tags.isNullOrEmpty().invert()) {
                if (tags!!.contains(tag)) {
                    database.getTagDao()!!.getTag(tag).apply {
                        packages = if (packages.contains("," + packageInfo.packageName)) {
                            packages.replace("," + packageInfo.packageName, "")
                        } else {
                            packages.replace(packageInfo.packageName, "")
                        }

                        database.getTagDao()!!.updateTag(this)
                    }
                }
            }

            withContext(Dispatchers.Main) {
                function()
                refresh()
            }
        }
    }

    fun refresh() {
        viewModelScope.launch(Dispatchers.IO) {
            loadTags()
            loadTagNames()
        }
    }

    fun deleteTag(tag: Tag) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            database?.getTagDao()?.deleteTag(tag)
            tags.value?.remove(tag)
            withContext(Dispatchers.Main) {
                refresh()
            }
        }
    }

    fun addMultipleAppsToTag(currentAppsList: java.util.ArrayList<BatchPackageInfo>, it: String, function: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsNameOnly()

            if (tags.isNullOrEmpty().invert()) {
                if (tags!!.contains(it)) {
                    database.getTagDao()!!.getTag(it).apply {
                        packages = packages.plus("," + currentAppsList.joinToString(",") {
                            it.packageInfo.packageName
                        })

                        // Remove duplicates
                        packages = packages.split(",").distinct().joinToString(",")

                        database.getTagDao()!!.updateTag(this)
                    }
                } else {
                    database.getTagDao()!!.insertTag(Tag(it, currentAppsList.joinToString(",") {
                        it.packageInfo.packageName
                    }, -1))
                }
            } else {
                database?.getTagDao()!!.insertTag(Tag(it, currentAppsList.joinToString(",") {
                    it.packageInfo.packageName
                }, -1))
            }

            withContext(Dispatchers.Main) {
                refresh()
                function()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun autoTag(storedFlags: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val database = TagsDatabase.getInstance(application.applicationContext)
            val tags = database?.getTagDao()?.getTagsNameOnly()
            val apps = getInstalledApps() + getUninstalledApps()
            val trackersData = TrackerUtils.getTrackersData()

            val flags = longArrayOf(
                    AutoTag.GAME,
                    AutoTag.AUDIO,
                    AutoTag.VIDEO,
                    AutoTag.IMAGE,
                    AutoTag.SOCIAL,
                    AutoTag.NEWS,
                    AutoTag.MAPS,
                    AutoTag.PRODUCTIVITY,
                    AutoTag.XPOSED_MODULE,
                    AutoTag.FOSS,
                    AutoTag.TRACKER)

            flags.forEach { flag ->
                if (FlagUtils.isFlagSet(storedFlags, flag)) {
                    val tag = getTagFromFlag(flag)
                    val filtered = apps.filter { it.doesAppHasFlag(flag, trackersData) }

                    if (filtered.isNotEmpty()) {
                        if (tags.isNullOrEmpty().invert()) {
                            if (tags!!.contains(tag)) {
                                database.getTagDao()!!.getTag(tag).apply {
                                    packages = packages.plus("," + filtered.joinToString(",") {
                                        it.packageName
                                    })

                                    // Remove duplicates
                                    packages = packages.split(",").distinct().joinToString(",")

                                    database.getTagDao()!!.updateTag(this)
                                }
                            } else {
                                database.getTagDao()!!.insertTag(Tag(tag, filtered.joinToString(",") {
                                    it.packageName
                                }, -1))
                            }
                        } else {
                            database?.getTagDao()!!.insertTag(Tag(tag, filtered.joinToString(",") {
                                it.packageName
                            }, -1))
                        }
                    }
                }
            }

            refresh()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun PackageInfo.doesAppHasFlag(flag: Long, trackersData: ArrayList<Tracker>): Boolean {
        return when (flag) {
            AutoTag.GAME -> {
                applicationInfo.category == ApplicationInfo.CATEGORY_GAME
            }
            AutoTag.AUDIO -> {
                applicationInfo.category == ApplicationInfo.CATEGORY_AUDIO
            }
            AutoTag.VIDEO -> {
                applicationInfo.category == ApplicationInfo.CATEGORY_VIDEO
            }
            AutoTag.IMAGE -> {
                applicationInfo.category == ApplicationInfo.CATEGORY_IMAGE
            }
            AutoTag.SOCIAL -> {
                applicationInfo.category == ApplicationInfo.CATEGORY_SOCIAL
            }
            AutoTag.NEWS -> {
                applicationInfo.category == ApplicationInfo.CATEGORY_NEWS
            }
            AutoTag.MAPS -> {
                applicationInfo.category == ApplicationInfo.CATEGORY_MAPS
            }
            AutoTag.PRODUCTIVITY -> {
                applicationInfo.category == ApplicationInfo.CATEGORY_PRODUCTIVITY
            }
            AutoTag.ACCESSIBILITY -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    applicationInfo.category == ApplicationInfo.CATEGORY_ACCESSIBILITY
                } else {
                    false
                }
            }
            AutoTag.XPOSED_MODULE -> {
                applicationInfo.isXposedModule()
            }
            AutoTag.FOSS -> {
                FOSSParser.isPackageFOSS(this)
            }
            AutoTag.TRACKER -> {
                getComponentsPackageInfo(applicationContext()).hasTrackers(trackersData)
            }
            else -> false
        }
    }

    private fun getTagFromFlag(flags: Long): String {
        return when {
            FlagUtils.isFlagSet(flags, AutoTag.GAME) -> getString(R.string.game)
            FlagUtils.isFlagSet(flags, AutoTag.AUDIO) -> getString(R.string.audio)
            FlagUtils.isFlagSet(flags, AutoTag.VIDEO) -> getString(R.string.video)
            FlagUtils.isFlagSet(flags, AutoTag.IMAGE) -> getString(R.string.image)
            FlagUtils.isFlagSet(flags, AutoTag.SOCIAL) -> getString(R.string.social)
            FlagUtils.isFlagSet(flags, AutoTag.NEWS) -> getString(R.string.news)
            FlagUtils.isFlagSet(flags, AutoTag.MAPS) -> getString(R.string.maps)
            FlagUtils.isFlagSet(flags, AutoTag.PRODUCTIVITY) -> getString(R.string.productivity)
            FlagUtils.isFlagSet(flags, AutoTag.ACCESSIBILITY) -> getString(R.string.accessibility)
            FlagUtils.isFlagSet(flags, AutoTag.XPOSED_MODULE) -> "Xposed_Module"
            FlagUtils.isFlagSet(flags, AutoTag.FOSS) -> getString(R.string.foss)
            FlagUtils.isFlagSet(flags, AutoTag.TRACKER) -> getString(R.string.trackers)
            else -> ""
        }
    }

    override fun onCleared() {
        TagsDatabase.destroyInstance()
        super.onCleared()
    }
}
