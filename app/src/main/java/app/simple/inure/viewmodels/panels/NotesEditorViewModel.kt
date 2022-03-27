package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.database.instances.NotesDatabase
import app.simple.inure.extension.viewmodels.WrappedViewModel
import app.simple.inure.models.NotesModel
import app.simple.inure.models.NotesPackageInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class NotesEditorViewModel(application: Application, private val packageInfo: PackageInfo) : WrappedViewModel(application) {

    private var notesDatabase: NotesDatabase? = null

    private val noteData: MutableLiveData<NotesPackageInfo> by lazy {
        MutableLiveData<NotesPackageInfo>().also {
            loadNoteData()
        }
    }

    private val saved = MutableLiveData<Int>()

    fun getNoteData(): LiveData<NotesPackageInfo> {
        return noteData
    }

    fun getSavedState(): LiveData<Int> {
        return saved
    }

    private fun loadNoteData() {
        viewModelScope.launch(Dispatchers.IO) {
            notesDatabase = NotesDatabase.getInstance(context)

            for (note in notesDatabase!!.getNotesDao()!!.getAllNotes()) {
                if (note.packageName == packageInfo.packageName) {
                    noteData.postValue(NotesPackageInfo(packageInfo, note.note, note.dateCreated, note.dateChanged))
                    break
                }
            }
        }
    }

    fun updateNoteData(notesPackageInfo: NotesPackageInfo, delay: Int = 0) {
        viewModelScope.launch(Dispatchers.IO) {
            delay(delay.toLong())

            notesDatabase!!.getNotesDao()!!
                .insertNote(NotesModel(
                        notesPackageInfo.note,
                        notesPackageInfo.packageInfo.packageName,
                        notesPackageInfo.dateCreated,
                        System.currentTimeMillis()))

            saved.postValue(saved.value?.plus(1) ?: 0)
        }
    }

    override fun onCleared() {
        super.onCleared()
        notesDatabase?.close()
    }
}