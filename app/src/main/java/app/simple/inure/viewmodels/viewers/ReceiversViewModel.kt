package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalled
import app.simple.inure.apk.utils.PackageUtils.safeApplicationInfo
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.preferences.SearchPreferences
import app.simple.inure.util.TrackerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ReceiversViewModel(application: Application, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val receivers: MutableLiveData<MutableList<ActivityInfoModel>> by lazy {
        MutableLiveData<MutableList<ActivityInfoModel>>().also {
            if (SearchPreferences.isSearchKeywordModeEnabled()) {
                getReceiversData(SearchPreferences.getLastSearchKeyword())
            } else {
                getReceiversData("")
            }
        }
    }

    fun getReceivers(): LiveData<MutableList<ActivityInfoModel>> {
        return receivers
    }

    fun getReceiversData(keyword: String) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                val list = arrayListOf<ActivityInfoModel>()
                val signatures = TrackerUtils.getTrackerSignatures()
                val isInstalled = packageManager.isPackageInstalled(packageInfo.packageName)

                for (ai in getPackageInfo(isInstalled).receivers!!) {
                    val activityInfoModel = ActivityInfoModel()

                    activityInfoModel.activityInfo = ai
                    activityInfoModel.name = ai.name
                    activityInfoModel.target = ai.targetActivity ?: application.getString(R.string.not_available)
                    activityInfoModel.exported = ai.exported
                    activityInfoModel.permission = ai.permission ?: application.getString(R.string.no_permissions_required)

                    for (signature in signatures) {
                        if (ai.name!!.contains(signature)) {
                            activityInfoModel.trackerId = signature
                            break
                        }
                    }

                    with(StringBuilder()) {
                        append(" | ")
                        append(MetaUtils.getLaunchMode(ai.launchMode, application))
                        append(" | ")
                        append(MetaUtils.getOrientation(ai.screenOrientation, application))
                        activityInfoModel.status = this.toString()
                    }

                    if (activityInfoModel.name.lowercase().contains(keyword.lowercase())) {
                        list.add(activityInfoModel)
                    }
                }

                list.sortBy {
                    it.name.substring(it.name.lastIndexOf(".") + 1)
                }

                receivers.postValue(list)
            }.getOrElse {
                if (it is NullPointerException) {
                    notFound.postValue(9)
                } else {
                    postError(it)
                }
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun getPackageInfo(isInstalled: Boolean): PackageInfo {
        return if (isInstalled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                packageManager.getPackageInfo(packageInfo.packageName,
                                              PackageManager.GET_RECEIVERS or PackageManager.MATCH_DISABLED_COMPONENTS)!!
            } else {
                packageManager.getPackageInfo(packageInfo.packageName,
                                              PackageManager.GET_RECEIVERS or PackageManager.GET_DISABLED_COMPONENTS)!!
            }
        } else {
            packageManager.getPackageArchiveInfo(packageInfo.safeApplicationInfo.sourceDir,
                                                 PackageManager.GET_RECEIVERS or PackageManager.GET_DISABLED_COMPONENTS)!!
        }
    }
}
