package app.simple.inure.viewmodels.panels

import android.app.Application
import android.content.pm.ApplicationInfo
import android.text.Spanned
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class XMLViewerData(application: Application, val applicationInfo: ApplicationInfo) : AndroidViewModel(application) {
    private val spanned: MutableLiveData<Spanned> by lazy {
        MutableLiveData<Spanned>().also {
            getSpanned()
        }
    }

    private fun getSpanned() {

    }
}