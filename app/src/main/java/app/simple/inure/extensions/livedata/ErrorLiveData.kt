package app.simple.inure.extensions.livedata

import android.app.Application
import android.content.Context
import androidx.lifecycle.MutableLiveData
import app.simple.inure.crash.Utils
import app.simple.inure.database.instances.StackTraceDatabase
import app.simple.inure.models.StackTrace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

open class ErrorLiveData : MutableLiveData<Throwable>() {

    override fun postValue(value: Throwable?) {
        super.postValue(value)
    }

    fun postError(value: Throwable, application: Application) {
        postValue(value)
        saveTraceToDatabase(value, application.applicationContext)
    }

    private fun saveTraceToDatabase(throwable: Throwable, applicationContext: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            StackTraceDatabase.init(applicationContext)
            throwable.printStackTrace()
            StackTraceDatabase.getInstance()
                ?.stackTraceDao()?.insertTrace(
                        StackTrace(throwable.toString(),
                                   Utils.getCause(throwable).message,
                                   Utils.getCause(throwable).toString(),
                                   System.currentTimeMillis()))
        }
    }
}