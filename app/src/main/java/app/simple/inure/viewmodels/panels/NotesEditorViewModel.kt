package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.PackageInfo
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.R
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

    private val formattingButtons: MutableLiveData<List<Int>> by lazy {
        MutableLiveData<List<Int>>().also {
            loadFormattingItems()
        }
    }

    private val saved = MutableLiveData<Int>()

    fun getNoteData(): LiveData<NotesPackageInfo> {
        return noteData
    }

    fun getFormattingStrip(): LiveData<List<Int>> {
        return formattingButtons
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
            kotlin.runCatching {
                delay(delay.toLong())

                notesDatabase!!.getNotesDao()!!
                    .insertNote(NotesModel(
                            notesPackageInfo.note,
                            notesPackageInfo.packageInfo.packageName,
                            notesPackageInfo.dateCreated,
                            System.currentTimeMillis()))

                saved.postValue(saved.value?.plus(1) ?: 0)
            }.onFailure {
                saved.postValue(-1 /* Save has failed, tell the UI */)
            }
        }
    }

    private fun loadFormattingItems() {
        viewModelScope.launch(Dispatchers.Default) {
            val list = listOf(
                    R.drawable.ic_format_bold,
                    R.drawable.ic_format_italic,
                    R.drawable.ic_format_underlined,
                    R.drawable.ic_format_strikethrough,
                    R.drawable.ic_format_size_lower,
                    R.drawable.ic_format_size_upper,
                    R.drawable.ic_format_list_bulleted,
                    R.drawable.ic_format_superscript,
                    R.drawable.ic_format_subscript)

            formattingButtons.postValue(list)
        }
    }

    fun refresh() {
        loadNoteData()
    }

    override fun onCleared() {
        super.onCleared()
        notesDatabase?.close()
    }
}