package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import android.text.Spannable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.model.ActivityInfoModel
import app.simple.inure.util.StringUtils.applyAccentColor
import app.simple.inure.util.StringUtils.applySecondaryTextColor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityInfoViewModel(application: Application, private val activityInfoModel: ActivityInfoModel, private val packageId: String, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val activityInfo: MutableLiveData<ArrayList<Pair<String, Spannable>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, Spannable>>>().also {
            loadData()
        }
    }

    fun getActivityInfo(): LiveData<ArrayList<Pair<String, Spannable>>> {
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

    private fun getLaunchMode(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.launch_mode),
                    MetaUtils.getLaunchMode(activityInfoModel.activityInfo.launchMode, getApplication()).applySecondaryTextColor(getApplication()))
    }

    private fun getOrientation(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.orientation),
                    MetaUtils.getOrientation(activityInfoModel.activityInfo.screenOrientation, getApplication()).applySecondaryTextColor(getApplication()))
    }

    private fun getSoftInputMode(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.soft_input_mode),
                    MetaUtils.getSoftInputMode(activityInfoModel.activityInfo.softInputMode, getApplication()).applyAccentColor())
    }

    private fun getColorMode(): Pair<String, Spannable> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Pair(getApplication<Application>().getString(R.string.color_mode),
                 MetaUtils.getColorMode(activityInfoModel.activityInfo.colorMode, getApplication()).applySecondaryTextColor(getApplication()))
        } else {
            Pair(getApplication<Application>().getString(R.string.soft_input_mode),
                 MetaUtils.getColorMode(-1, getApplication()).applySecondaryTextColor(getApplication()))
        }
    }

    private fun getDocumentLaunchMode(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.document_launch_mode),
                    MetaUtils.getDocumentLaunchMode(activityInfoModel.activityInfo.documentLaunchMode, getApplication()).applySecondaryTextColor(getApplication()))
    }

    private fun getPersistableMode(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.persistable_mode),
                    MetaUtils.getPersistableMode(activityInfoModel.activityInfo.persistableMode, getApplication()).applySecondaryTextColor(getApplication()))
    }

    private fun getFlags(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.flags),
                    MetaUtils.getFlags(activityInfoModel.activityInfo.flags, getApplication()).applyAccentColor())
    }

    private fun getConfigurationsChanges(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.configuration_changes),
                    MetaUtils.getConfigurationsChanges(activityInfoModel.activityInfo.configChanges, getApplication()).applyAccentColor())
    }

    private fun getTaskAffinity(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.task_affinity),
                    activityInfoModel.activityInfo.taskAffinity.applySecondaryTextColor(getApplication()))
    }

    private fun getParentActivity(): Pair<String, Spannable> {
        return Pair(getApplication<Application>().getString(R.string.parent_activity),
                    activityInfoModel.activityInfo.parentActivityName?.applySecondaryTextColor(getApplication())
                        ?: getApplication<Application>().getString(R.string.none).applySecondaryTextColor(getApplication()))
    }
}