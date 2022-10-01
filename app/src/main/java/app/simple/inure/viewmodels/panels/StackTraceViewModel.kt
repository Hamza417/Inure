package app.simple.inure.viewmodels.panels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.database.instances.StackTraceDatabase
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.models.StackTrace
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class StackTraceViewModel(application: Application) : WrappedViewModel(application) {

    private val stackTraces: MutableLiveData<ArrayList<StackTrace>> by lazy {
        MutableLiveData<ArrayList<StackTrace>>().also {
            loadTraces()
        }
    }

    fun getStackTraces(): LiveData<ArrayList<StackTrace>> {
        return stackTraces
    }

    private fun loadTraces() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val db = StackTraceDatabase.getInstance(applicationContext())
                stackTraces.postValue(db?.stackTraceDao()?.getStackTraces() as ArrayList<StackTrace>)
            }.getOrElse {
                error.postValue(it)
            }
        }
    }
}