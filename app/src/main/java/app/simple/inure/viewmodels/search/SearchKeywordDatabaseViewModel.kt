package app.simple.inure.viewmodels.search

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PermissionUtils
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SearchKeywordDatabaseViewModel(application: Application) : WrappedViewModel(application) {

    private val permissions: MutableLiveData<List<String>> by lazy {
        MutableLiveData<List<String>>().also {
            viewModelScope.launch(Dispatchers.Default) {
                permissions.postValue(PermissionUtils.getAndroidPermissionList())
            }
        }
    }

    private val trackers: MutableLiveData<ArrayList<String>> by lazy {
        MutableLiveData<ArrayList<String>>().also {

        }
    }

    fun getPermissions(): LiveData<List<String>> {
        return permissions
    }

    fun getTrackers(): LiveData<ArrayList<String>> {
        return trackers
    }
}
