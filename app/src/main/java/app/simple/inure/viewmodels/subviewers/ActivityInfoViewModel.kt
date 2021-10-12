package app.simple.inure.viewmodels.subviewers

import android.app.Application
import android.content.pm.PackageInfo
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.apk.utils.MetaUtils
import app.simple.inure.model.ActivityInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityInfoViewModel(application: Application, private val activityInfoModel: ActivityInfoModel, private val packageId: String, val packageInfo: PackageInfo) : AndroidViewModel(application) {

    private val activityInfo: MutableLiveData<ArrayList<Pair<String, String>>> by lazy {
        MutableLiveData<ArrayList<Pair<String, String>>>().also {
            loadData()
        }
    }

    fun getActivityInfo(): LiveData<ArrayList<Pair<String, String>>> {
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
                getConfigurationsChanges()
            ))
        }

        activityInfoModel.activityInfo.configChanges
    }

    private fun getLaunchMode(): Pair<String, String> {
        return Pair(getApplication<Application>().getString(R.string.launch_mode),
                    MetaUtils.getLaunchMode(activityInfoModel.activityInfo.launchMode, getApplication()))
    }

    private fun getOrientation(): Pair<String, String> {
        return Pair(getApplication<Application>().getString(R.string.orientation),
                    MetaUtils.getOrientation(activityInfoModel.activityInfo.screenOrientation, getApplication()))
    }

    private fun getSoftInputMode(): Pair<String, String> {
        return Pair(getApplication<Application>().getString(R.string.soft_input_mode),
                    MetaUtils.getSoftInputString(activityInfoModel.activityInfo.softInputMode, getApplication()))
    }

    private fun getColorMode(): Pair<String, String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Pair(getApplication<Application>().getString(R.string.color_mode),
                 MetaUtils.getColorMode(activityInfoModel.activityInfo.colorMode, getApplication()))
        } else {
            Pair(getApplication<Application>().getString(R.string.soft_input_mode),
                 MetaUtils.getColorMode(-1, getApplication()))
        }
    }

    private fun getDocumentLaunchMode(): Pair<String, String> {
        return Pair(getApplication<Application>().getString(R.string.document_launch_mode),
                    MetaUtils.getDocumentLaunchMode(activityInfoModel.activityInfo.documentLaunchMode, getApplication()))
    }

    private fun getPersistableMode(): Pair<String, String> {
        return Pair(getApplication<Application>().getString(R.string.persistable_mode),
                    MetaUtils.getPersistableMode(activityInfoModel.activityInfo.persistableMode, getApplication()))
    }

    private fun getFlags(): Pair<String, String> {
        return Pair(getApplication<Application>().getString(R.string.flags),
                    MetaUtils.getFlags(activityInfoModel.activityInfo.flags, getApplication()))
    }

    private fun getConfigurationsChanges(): Pair<String, String> {
        return Pair(getApplication<Application>().getString(R.string.configuration_changes),
                    MetaUtils.getConfigurationsChanges(activityInfoModel.activityInfo.configChanges, getApplication()))
    }
}