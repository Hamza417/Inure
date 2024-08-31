package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.text.SpannableStringBuilder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.database.instances.NotesDatabase
import app.simple.inure.extensions.viewmodels.PackageUtilsViewModel
import app.simple.inure.models.Notes
import app.simple.inure.models.Note
import app.simple.inure.text.SpannableSerializer
import app.simple.inure.ui.editor.NotesEditor
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.reflect.Type

class NotesViewModel(application: Application) : PackageUtilsViewModel(application) {

    private var notesDatabase: NotesDatabase? = null
    private var broadcastReceiver: BroadcastReceiver? = null
    private val intentFilter = IntentFilter(NotesEditor.NOTES_UPDATED)

    init {
        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == NotesEditor.NOTES_UPDATED) {
                    loadNotesData()
                }
            }
        }

        LocalBroadcastManager.getInstance(application).registerReceiver(broadcastReceiver!!, intentFilter)
    }

    private val gson: Gson by lazy {
        val type: Type = object : TypeToken<SpannableStringBuilder>() {}.type
        GsonBuilder().registerTypeAdapter(type, SpannableSerializer()).create()
    }

    private val notesData: MutableLiveData<ArrayList<Note>> by lazy {
        MutableLiveData<ArrayList<Note>>()
    }

    private val delete = MutableLiveData<Int?>()

    fun getNotesData(): LiveData<ArrayList<Note>> {
        return notesData
    }

    fun getDelete(): LiveData<Int?> {
        return delete
    }

    private fun loadNotesData() {
        viewModelScope.launch(Dispatchers.Default) {
            notesData.postValue(getNotesData(getInstalledApps()))
        }
    }

    private suspend fun getNotesData(apps: ArrayList<PackageInfo>): ArrayList<Note> {
        notesDatabase = NotesDatabase.getInstance(context)

        val list = arrayListOf<Note>()

        for (note in notesDatabase!!.getNotesDao()!!.getAllNotes()) {
            for (app in apps) {
                if (note.packageName == app.packageName) {
                    list.add(Note(
                            app,
                            gson.fromJson(note.note, SpannableStringBuilder::class.java),
                            note.dateCreated,
                            note.dateChanged
                    ))

                    break
                }
            }
        }

        return list
    }

    fun deleteNoteData(note: Note?, position: Int) {
        viewModelScope.launch(Dispatchers.Default) {
            kotlin.runCatching {
                notesDatabase = NotesDatabase.getInstance(context)

                val notes = Notes(
                        gson.toJson(note!!.note),
                        note.packageInfo.packageName,
                        note.dateCreated,
                        note.dateUpdated)

                notesDatabase?.getNotesDao()?.deleteNote(notes)
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

    override fun onAppsLoaded(apps: ArrayList<PackageInfo>) {
        super.onAppsLoaded(apps)
        loadNotesData()
    }

    fun refreshNotes() {
        loadNotesData()
    }

    override fun onCleared() {
        super.onCleared()
        kotlin.runCatching {
            notesDatabase?.close()
            LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver!!)
        }
    }

    fun clearDelete() {
        delete.postValue(null)
    }
}
