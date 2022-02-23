package app.simple.inure.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TextDataViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * Declare the mutable live data object, this will be the
     * data holder for the view.
     */
    private val text: MutableLiveData<String> by lazy {
        MutableLiveData<String>().also {
            loadTextData()
        }
    }

    /**
     * Getter function to prevent the data from being
     * modified by the other class. We'll also be returning
     * an immutable [LiveData] object for the same reason.
     */
    fun getText(): LiveData<String> {
        return text
    }

    private fun loadTextData() {
        viewModelScope.launch(Dispatchers.IO) {
            // Load text data here
            val p0: String? = "This is a text"

            /**
             * Post the data to the LiveData to be sent
             * to the observers.
             */
            text.postValue(p0)
        }
    }
}