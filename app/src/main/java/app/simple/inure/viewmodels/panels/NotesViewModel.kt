package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.database.instances.NotesDatabase
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.models.NotesPackageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotesViewModel(application: Application) : WrappedViewModel(application) {

    private var notesDatabase: NotesDatabase? = null

    private val notesData: MutableLiveData<ArrayList<NotesPackageInfo>> by lazy {
        MutableLiveData<ArrayList<NotesPackageInfo>>().also {
            loadNotesData()
        }
    }

    fun getNotesData(): LiveData<ArrayList<NotesPackageInfo>> {
        return notesData
    }

    private fun loadNotesData() {
        viewModelScope.launch(Dispatchers.Default) {
            val apps = packageManager.getInstalledPackages(PackageManager.GET_META_DATA) as ArrayList
            notesData.postValue(getNotesData(apps))
        }
    }

    private suspend fun getNotesData(apps: ArrayList<PackageInfo>): ArrayList<NotesPackageInfo> {
        notesDatabase = NotesDatabase.getInstance(context)

        val list = arrayListOf<NotesPackageInfo>()

        for (note in notesDatabase!!.getNotesDao()!!.getAllNotes()) {
            for (app in apps) {
                if (note.packageName == app.packageName) {
                    list.add(NotesPackageInfo(
                            app,
                            note.note,
                            note.dateCreated,
                            note.dateChanged
                    ))

                    break
                }
            }
        }

        for (app in list) {
            app.packageInfo.applicationInfo.name =
                PackageUtils.getApplicationName(getApplication<Application>().applicationContext, app.packageInfo.applicationInfo)
        }

        return list
    }

    override fun onCleared() {
        super.onCleared()
        notesDatabase?.close()
    }
}