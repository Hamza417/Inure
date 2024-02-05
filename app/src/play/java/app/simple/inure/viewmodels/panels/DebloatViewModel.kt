package app.simple.inure.viewmodels.panels

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.simple.inure.extensions.viewmodels.RootShizukuViewModel

class DebloatViewModel(application: Application) : RootShizukuViewModel(application) {

    private val bloatList: MutableLiveData<ArrayList<Any>> by lazy {
        MutableLiveData<ArrayList<Any>>()
    }

    fun getBloatList(): LiveData<ArrayList<Any>> {
        return bloatList
    }
}
