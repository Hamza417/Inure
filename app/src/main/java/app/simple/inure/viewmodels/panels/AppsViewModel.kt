package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.constants.SortConstant
import app.simple.inure.events.AppsEvent
import app.simple.inure.extensions.viewmodels.DataGeneratorViewModel
import app.simple.inure.preferences.AppsPreferences
import app.simple.inure.util.ArrayUtils.toArrayList
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.Sort.getSortedList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.stream.Collectors

class AppsViewModel(application: Application) : DataGeneratorViewModel(application) {

    private val appData: MutableLiveData<ArrayList<PackageInfo>> by lazy {
        MutableLiveData<ArrayList<PackageInfo>>()
    }

    val appLoaded: MutableLiveData<AppsEvent<Boolean>> by lazy {
        MutableLiveData<AppsEvent<Boolean>>()
    }

    fun getAppData(): LiveData<ArrayList<PackageInfo>> {
        return appData
    }

    fun isAppDataEmpty(): Boolean {
        return appData.value.isNullOrEmpty()
    }

    @Suppress("UNCHECKED_CAST")
    fun loadAppData() {
        viewModelScope.launch(Dispatchers.Default) {
            var apps = (getInstalledApps() + getUninstalledApps()).toArrayList()

            when (AppsPreferences.getAppsType()) {
                SortConstant.SYSTEM -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
                SortConstant.USER -> {
                    apps = apps.stream().filter { p ->
                        p.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
                    }.collect(Collectors.toList()) as ArrayList<PackageInfo>
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val categoryList = ArrayList<PackageInfo>()

                for (app in apps) {
                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_UNDEFINED) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_UNSPECIFIED)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_GAME) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_GAME)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_AUDIO) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_AUDIO)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_VIDEO) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_VIDEO)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_IMAGE) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_IMAGE)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_SOCIAL) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_SOCIAL)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_NEWS) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_NEWS)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_MAPS) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_MAPS)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (app.applicationInfo.category == ApplicationInfo.CATEGORY_PRODUCTIVITY) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_PRODUCTIVITY)) {
                            if (!categoryList.contains(app)) {
                                categoryList.add(app)
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        if (app.applicationInfo.category == ApplicationInfo.CATEGORY_ACCESSIBILITY) {
                            if (FlagUtils.isFlagSet(AppsPreferences.getAppsCategory(), SortConstant.CATEGORY_ACCESSIBILITY)) {
                                if (!categoryList.contains(app)) {
                                    categoryList.add(app)
                                }
                            }
                        }
                    }
                }

                apps = categoryList.stream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo> // Unnecessary ??
            }

            var filteredList = arrayListOf<PackageInfo>()

            if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.COMBINE_FLAGS)) { // Pretty special case, even I don't know what I did here
                filteredList.addAll((apps.clone() as ArrayList<PackageInfo>).stream().filter { p ->
                    if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                            true
                        } else {
                            p.applicationInfo.enabled.invert()
                        }
                    } else {
                        true
                    } && if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                            true
                        } else {
                            p.applicationInfo.enabled
                        }
                    } else {
                        true
                    } && if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.APK)) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                            true
                        } else {
                            p.applicationInfo.splitSourceDirs.isNullOrEmpty()
                        }
                    } else {
                        true
                    } && if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                        if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.APK)) {
                            true
                        } else {
                            p.applicationInfo.splitSourceDirs?.isNotEmpty() ?: false
                        }
                    } else {
                        true
                    } && if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                        p.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0
                    } else {
                        true
                    }
                }.collect(Collectors.toList()) as ArrayList<PackageInfo>)
            } else {
                for (app in apps) {
                    if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.UNINSTALLED)) {
                        if (app.applicationInfo.flags and ApplicationInfo.FLAG_INSTALLED == 0) {
                            if (!filteredList.contains(app)) {
                                filteredList.add(app)
                            }
                        }
                    }

                    if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.SPLIT)) {
                        if (app.applicationInfo.splitSourceDirs?.isNotEmpty() == true) {
                            if (!filteredList.contains(app)) {
                                filteredList.add(app)
                            }
                        }
                    }

                    if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.DISABLED)) {
                        if (!app.applicationInfo.enabled) {
                            if (!filteredList.contains(app)) {
                                filteredList.add(app)
                            }
                        }
                    }

                    if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.APK)) {
                        if (app.applicationInfo.splitSourceDirs.isNullOrEmpty()) {
                            if (!filteredList.contains(app)) {
                                filteredList.add(app)
                            }
                        }
                    }

                    if (FlagUtils.isFlagSet(AppsPreferences.getAppsFilter(), SortConstant.ENABLED)) {
                        if (app.applicationInfo.enabled) {
                            if (!filteredList.contains(app)) {
                                filteredList.add(app)
                            }
                        }
                    }
                }

                // Remove duplicate elements
                filteredList = filteredList.stream().distinct().collect(Collectors.toList()) as ArrayList<PackageInfo>
            }

            filteredList.getSortedList(AppsPreferences.getSortStyle(), AppsPreferences.isReverseSorting())

            appData.postValue(filteredList as ArrayList<PackageInfo>?)
            appLoaded.postValue(AppsEvent(true))
        }
    }

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        loadAppData()
    }

    override fun onAppUninstalled(packageName: String?) {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = appData.value

            if (apps != null) {
                for (i in apps.indices) {
                    if (apps[i].packageName == packageName) {
                        apps.removeAt(i)
                        break
                    }
                }

                appData.postValue(apps)
            }
        }
    }
}