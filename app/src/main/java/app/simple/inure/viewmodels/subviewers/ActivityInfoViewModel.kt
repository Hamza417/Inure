package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import android.text.Spannable
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.ActivityInfoModel
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityInfoViewModel(application: Application, private val activityInfoModel: ActivityInfoModel, val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private val activityInfo: MutableLiveData<ArrayList<Pair<Int, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<Int, Spannable>>>().also {
            loadData()
        }
    }

    fun getActivityInfo(): LiveData<ArrayList<Pair<Int, Spannable>>> {
        return activityInfo
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            activityInfo.postValue(arrayListOf(
                    getLaunchMode(),
                    getOrientation(),
                    getSoftInputMode(),
                    getColorMode(),
                    getDocumentLaunchMode(),
                    getPersistableMode(),
                    getFlags(),
                    getConfigurationsChanges(),
                    getTaskAffinity(),
                    getParentActivity()
            ))
        }
    }

    private fun getLaunchMode(): Pair<Int, Spannable> {
        return Pair(R.string.launch_mode,
                    MetaUtils.getLaunchMode(activityInfoModel.activityInfo.launchMode, getApplication()).applySecondaryTextColor())
    }

    private fun getOrientation(): Pair<Int, Spannable> {
        return Pair(R.string.orientation,
                    MetaUtils.getOrientation(activityInfoModel.activityInfo.screenOrientation, getApplication()).applySecondaryTextColor())
    }

    private fun getSoftInputMode(): Pair<Int, Spannable> {
        return Pair(R.string.soft_input_mode,
                    MetaUtils.getSoftInputMode(activityInfoModel.activityInfo.softInputMode, getApplication()).applyAccentColor())
    }

    private fun getColorMode(): Pair<Int, Spannable> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Pair(R.string.color_mode,
                 MetaUtils.getColorMode(activityInfoModel.activityInfo.colorMode, getApplication()).applySecondaryTextColor())
        } else {
            Pair(R.string.soft_input_mode,
                 MetaUtils.getColorMode(-1, getApplication()).applySecondaryTextColor())
        }
    }

    private fun getDocumentLaunchMode(): Pair<Int, Spannable> {
        return Pair(R.string.document_launch_mode,
                    MetaUtils.getDocumentLaunchMode(activityInfoModel.activityInfo.documentLaunchMode, getApplication()).applySecondaryTextColor())
    }

    private fun getPersistableMode(): Pair<Int, Spannable> {
        return Pair(R.string.persistable_mode,
                    MetaUtils.getPersistableMode(activityInfoModel.activityInfo.persistableMode, getApplication()).applySecondaryTextColor())
    }

    private fun getFlags(): Pair<Int, Spannable> {
        return Pair(R.string.flags,
                    MetaUtils.getFlags(activityInfoModel.activityInfo.flags, getApplication()).applyAccentColor())
    }

    private fun getConfigurationsChanges(): Pair<Int, Spannable> {
        return Pair(R.string.configuration_changes,
                    MetaUtils.getConfigurationsChanges(activityInfoModel.activityInfo.configChanges, getApplication()).applyAccentColor())
    }

    private fun getTaskAffinity(): Pair<Int, Spannable> {
        kotlin.runCatching {
            return Pair(R.string.task_affinity,
                        activityInfoModel.activityInfo.taskAffinity.applySecondaryTextColor())
        }.getOrElse {
            return Pair(R.string.task_affinity,
                        getString(R.string.not_available).applySecondaryTextColor())
        }
    }

    private fun getParentActivity(): Pair<Int, Spannable> {
        return Pair(R.string.parent_activity,
                    activityInfoModel.activityInfo.parentActivityName?.applySecondaryTextColor()
                        ?: getString(R.string.none).applySecondaryTextColor())
    }
}