package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import android.text.SpannableStringBuilder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.apk.utils.PackageUtils.getInstalledPackages
import app.simple.inure.database.instances.NotesDatabase
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.NotesModel
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.text.SpannableSerializer
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Type

class NotesViewModel(application: Application) : WrappedViewModel(application) {

    private var notesDatabase: NotesDatabase? = null

    private val gson: Gson by lazy {
        val type: Type = object : TypeToken<SpannableStringBuilder>() {}.type
        GsonBuilder().registerTypeAdapter(type, SpannableSerializer()).create()
    }

    private val notesData: MutableLiveData<ArrayList<NotesPackageInfo>> by lazy {
        MutableLiveData<ArrayList<NotesPackageInfo>>().also {
            loadNotesData()
        }
    }

    private val delete = MutableLiveData<Int>()

    fun getNotesData(): LiveData<ArrayList<NotesPackageInfo>> {
        return notesData
    }

    fun getDelete(): LiveData<Int> {
        return delete
    }

    private fun loadNotesData() {
        viewModelScope.launch(Dispatchers.Default) {
            notesData.postValue(getNotesData(packageManager.getInstalledPackages()))
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
                            gson.fromJson(note.note, SpannableStringBuilder::class.java),
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

    fun deleteNoteData(notesPackageInfo: NotesPackageInfo?, position: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                notesDatabase = NotesDatabase.getInstance(context)

                val notesModel = NotesModel(
                        gson.toJson(notesPackageInfo!!.note),
                        notesPackageInfo.packageInfo.packageName,
                        notesPackageInfo.dateCreated,
                        notesPackageInfo.dateUpdated)

                notesDatabase?.getNotesDao()?.deleteNote(notesModel)
                delete.postValue(position)
            }.getOrElse {
                postError(it)
            }
        }
    }

    override fun onAppUninstalled(packageName: String?) {
        super.onAppUninstalled(packageName)
        loadNotesData()
    }

    fun refreshNotes() {
        loadNotesData()
    }

    override fun onCleared() {
        super.onCleared()
        notesDatabase?.close()
    }
}