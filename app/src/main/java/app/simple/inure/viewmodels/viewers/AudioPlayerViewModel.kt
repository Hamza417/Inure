package app.simple.inure.viewmodels.viewers

import android.app.Application
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import app.simple.inure.extensions.viewmodels.WrappedViewModel
import app.simple.inure.loaders.MetadataHelper
import app.simple.inure.models.AudioModel

class AudioPlayerViewModel(application: Application, private val uri: Uri) : WrappedViewModel(application) {

    private val audioModels: MutableLiveData<ArrayList<AudioModel>> by lazy {
        MutableLiveData<ArrayList<AudioModel>>().also {
            loadAudio()
        }
    }

    fun getAudioModels(): LiveData<ArrayList<AudioModel>> {
        return audioModels
    }

    /**
     * Queries the content provider for all the audio files under the
     * given uri and returns a list of audio models
     */
    private fun loadAudio() {
        val metadata = MetadataHelper.getAudioMetadata(getApplication(), uri)
        val audioModel = AudioModel()

        audioModel.title = metadata.title
        audioModel.artists = metadata.artists
        audioModel.album = metadata.album
        audioModel.fileUri = uri.toString()

        audioModels.postValue(arrayListOf(audioModel))
    }
}