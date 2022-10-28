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

    private val delete = MutableLiveData<Int>()

    fun getStackTraces(): LiveData<ArrayList<StackTrace>> {
        return stackTraces
    }

    fun getDelete(): LiveData<Int> {
        return delete
    }

    private fun loadTraces() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val db = StackTraceDatabase.getInstance(applicationContext())
                stackTraces.postValue(db?.stackTraceDao()?.getStackTraces() as ArrayList<StackTrace>)
            }.getOrElse {
                postError(it)
            }
        }
    }

    fun deleteStackTrace(stackTrace: StackTrace, position: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                val db = StackTraceDatabase.getInstance(applicationContext())
                db?.stackTraceDao()?.deleteStackTrace(stackTrace)
                delete.postValue(position)
            }.getOrElse {
                postError(it)
            }
        }
    }
}