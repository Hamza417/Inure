package app.simple.inure.viewmodels.dialogs

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
import app.simple.inure.database.instances.BatchProfileDatabase
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.BatchProfile
import app.simple.inure.preferences.BatchPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BatchProfilesViewModel(application: Application) : WrappedViewModel(application) {

    private val profiles: MutableLiveData<ArrayList<BatchProfile>> by lazy {
        MutableLiveData<ArrayList<BatchProfile>>().also {
            loadProfiles()
        }
    }

    fun getProfiles(): LiveData<ArrayList<BatchProfile>> {
        return profiles
    }

    private fun loadProfiles() {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                BatchProfileDatabase.getInstance(application)?.batchProfileDao().let { dao ->
                    dao?.getBatchProfiles()?.let { profiles ->
                        if (profiles.isNotEmpty()) {
                            this@BatchProfilesViewModel.profiles.postValue(profiles as ArrayList<BatchProfile>)
                        } else {
                            val defaultProfile = BatchProfile(getString(R.string.default_identifier),
                                                              "",
                                                              BatchPreferences.getAppsFilter(),
                                                              BatchPreferences.getSortStyle(),
                                                              BatchPreferences.isReverseSorting(),
                                                              BatchPreferences.getAppsCategory(),
                                                              System.currentTimeMillis())

                            defaultProfile.id = -1

                            this@BatchProfilesViewModel.profiles.postValue(arrayListOf(defaultProfile))
                        }
                    }
                }
            }
        }
    }

    fun deleteProfile(profile: BatchProfile, function: () -> Unit = {}) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                BatchProfileDatabase.getInstance(application)?.batchProfileDao().let { dao ->
                    dao?.deleteBatchProfile(profile.id)
                    withContext(Dispatchers.Main) {
                        function()
                    }
                }
            }
        }
    }
}
