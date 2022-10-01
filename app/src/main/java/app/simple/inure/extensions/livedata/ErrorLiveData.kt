package app.simple.inure.extensions.livedata

import androidx.lifecycle.MutableLiveData
import app.simple.inure.database.instances.StackTraceDatabase
import app.simple.inure.models.StackTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ErrorLiveData : MutableLiveData<String>() {
    override fun postValue(value: String?) {
        super.postValue(value)
        CoroutineScope(Dispatchers.IO).launch {
            StackTraceDatabase.getInstance()
                ?.stackTraceDao()?.insertTrace(StackTrace(value!!, System.currentTimeMillis()))
        }
    }
}