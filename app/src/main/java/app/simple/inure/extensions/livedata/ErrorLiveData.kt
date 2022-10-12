package app.simple.inure.extensions.livedata

import androidx.lifecycle.MutableLiveData
import app.simple.inure.database.instances.StackTraceDatabase
import app.simple.inure.models.StackTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class ErrorLiveData : MutableLiveData<Throwable>() {
    override fun postValue(value: Throwable) {
        super.postValue(value)
        saveTraceToDatabase(value)
    }

    private fun saveTraceToDatabase(throwable: Throwable) {
        CoroutineScope(Dispatchers.IO).launch {
            println(throwable.toString())
            StackTraceDatabase.getInstance()
                ?.stackTraceDao()?.insertTrace(StackTrace(throwable))
        }
    }
}