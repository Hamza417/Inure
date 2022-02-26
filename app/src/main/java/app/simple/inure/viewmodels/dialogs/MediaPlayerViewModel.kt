package app.simple.inure.viewmodels.dialogs

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import app.simple.inure.models.AudioMetaData
import app.simple.inure.util.MetadataHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.ln

/**
 * Use [app.simple.inure.services.AudioService] to play and manage the
 * states of audio files.
 */
@Deprecated("")
class MediaPlayerViewModel(application: Application, private val uri: Uri?) :
        AndroidViewModel(application),
        AudioManager.OnAudioFocusChangeListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnSeekCompleteListener {

    private val mediaPlayer = MediaPlayer()
    private var handler = Handler(Looper.getMainLooper())
    private var audioManager = getApplication<Application>().getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var focusRequest: AudioFocusRequest? = null
    private val audioBecomingNoisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private val volumeFadeDuration: Int = 250
    private var iVolume = 0
    private val intVolumeMax = 100
    private val intVolumeMin = 0
    private val floatVolumeMax = 1f
    private val floatVolumeMin = 0f
    private val durationSmoother = 1000

    private var wasPlaying = false

    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

        }
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)
                .build()
        }

        getApplication<Application>().registerReceiver(becomingNoisyReceiver, audioBecomingNoisyFilter)

        audioPlayer()
    }

    private val error: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    private val progress: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private val duration: MutableLiveData<Int> by lazy {
        MutableLiveData<Int>()
    }

    private val state: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    private val audioMetaData: MutableLiveData<AudioMetaData> by lazy {
        MutableLiveData<AudioMetaData>().also {
            loadAudioData()
        }
    }

    private val close: MutableLiveData<Boolean> by lazy {
        MutableLiveData<Boolean>()
    }

    fun getCloseEvent(): LiveData<Boolean> {
        return close
    }

    fun getMetadata(): LiveData<AudioMetaData> {
        return audioMetaData
    }

    fun getProgress(): LiveData<Int> {
        return progress
    }

    fun getDuration(): LiveData<Int> {
        return duration
    }

    fun getState(): LiveData<Boolean> {
        return state
    }

    fun getError(): LiveData<String> {
        return error
    }

    fun seek(position: Int) {
        mediaPlayer.seekTo(position)
    }

    fun removeProgressCallbacks() {
        handler.removeCallbacks(progressRunnable)
    }

    private fun postProgressCallbacks() {
        removeProgressCallbacks()
        handler.post(progressRunnable)
    }

    fun changePlayerState() {
        if (mediaPlayer.isPlaying) {
            pause()
            wasPlaying = false
            state.postValue(false)
        } else {
            play()
            wasPlaying = true
            state.postValue(true)
        }
    }

    private fun loadAudioData() {
        viewModelScope.launch(Dispatchers.IO) {
            kotlin.runCatching {
                audioMetaData.postValue(
                        MetadataHelper.getAudioMetadata(getApplication<Application>(), uri!!)
                )
            }.getOrElse {
                error.postValue(it.message!!)
            }
        }
    }

    private fun audioPlayer() {
        kotlin.runCatching {
            mediaPlayer.setOnCompletionListener(this)
            mediaPlayer.setOnPreparedListener(this)
            mediaPlayer.setOnSeekCompleteListener(this)
            mediaPlayer.setDataSource(getApplication<Application>().applicationContext, uri!!)
            mediaPlayer.prepareAsync()
        }.getOrElse {
            error.postValue(it.message!!)
        }
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mediaPlayer.isPlaying) {
                    if (wasPlaying) {
                        mediaPlayer.start()
                    }
                }
            }
            /**
             * Lost focus for an unbounded amount of time: stop playback and release media player
             */
            AudioManager.AUDIOFOCUS_LOSS,
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT,
            -> {

                wasPlaying = mediaPlayer.isPlaying

                /**
                 * Lost focus for a short time, but we have to stop
                 * playback. We don't release the media player because playback
                 * is likely to resume
                 */
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.pause()
                }
            }
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.setVolume(.1f, .1f)
                }
            }
        }
    }

    override fun onCompletion(mp: MediaPlayer?) {
        removeProgressCallbacks()
        close.postValue(true)
    }

    override fun onPrepared(mp: MediaPlayer) {
        if (requestAudioFocus()) {
            mp.start()
            duration.postValue(mp.duration.times(durationSmoother))
            postProgressCallbacks()
            state.postValue(true)
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        return true
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        postProgressCallbacks()
    }

    private val progressRunnable: Runnable = object : Runnable {
        override fun run() {
            progress.postValue(mediaPlayer.currentPosition.times(durationSmoother))
            handler.postDelayed(this, 1000L)
        }
    }

    private fun requestAudioFocus(): Boolean {
        val value: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            value = audioManager.requestAudioFocus(focusRequest!!)
        } else {
            @Suppress("deprecation") // Required for older APIs
            value = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
        }

        return value == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioManager.abandonAudioFocusRequest(focusRequest!!)
            } else {
                @Suppress("Deprecation")
                audioManager.abandonAudioFocus(this)
            }
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }
    }

    private fun pause() {
        if (timerTask != null && timer != null) {
            timer!!.cancel()
            timerTask!!.cancel()
        }

        // Set current volume, depending on fade or not
        iVolume = if (volumeFadeDuration > 0) {
            intVolumeMax
        } else {
            intVolumeMin
        }
        updateVolume(0)

        // Start increasing volume in increments
        if (volumeFadeDuration > 0) {
            timer = Timer(true)
            timerTask = object : TimerTask() {
                override fun run() {
                    updateVolume(-1)
                    if (iVolume == intVolumeMin) {
                        // Pause music
                        if (mediaPlayer.isPlaying) {
                            mediaPlayer.pause()
                        }
                        timer!!.cancel()
                        timer!!.purge()
                    }
                }
            }

            // calculate delay, cannot be zero, set to 1 if zero
            var delay: Int = volumeFadeDuration / intVolumeMax
            if (delay == 0) {
                delay = 1
            }
            timer!!.schedule(timerTask, delay.toLong(), delay.toLong())
        }
    }

    private fun play() {
        if (timerTask != null && timer != null) {
            timer!!.cancel()
            timerTask!!.cancel()
        }

        // Set current volume, depending on fade or not
        iVolume = if (volumeFadeDuration > 0) {
            intVolumeMin
        } else {
            intVolumeMax
        }

        updateVolume(0)

        // Play music
        if (!mediaPlayer.isPlaying) {
            if (requestAudioFocus()) {
                mediaPlayer.start()
            }
        }

        // Start increasing volume in increments
        if (volumeFadeDuration > 0) {
            timer = Timer(true)
            timerTask = object : TimerTask() {
                override fun run() {
                    updateVolume(1)
                    if (iVolume == intVolumeMax) {
                        timer!!.cancel()
                        timer!!.purge()
                    }
                }
            }

            // calculate delay, cannot be zero, set to 1 if zero
            var delay: Int = volumeFadeDuration / intVolumeMax
            if (delay == 0) {
                delay = 1
            }
            timer!!.schedule(timerTask, delay.toLong(), delay.toLong())
        }
    }

    private fun updateVolume(change: Int) {
        // increment or decrement depending on type of fade
        iVolume += change

        // ensure iVolume within boundaries
        if (iVolume < intVolumeMin) {
            iVolume = intVolumeMin
        } else if (iVolume > intVolumeMax) {
            iVolume = intVolumeMax
        }

        // convert to float value
        var fVolume = 1 - ln((intVolumeMax - iVolume).toDouble()).toFloat() / ln(intVolumeMax.toDouble()).toFloat()

        // ensure fVolume within boundaries
        if (fVolume < floatVolumeMin) {
            fVolume = floatVolumeMin
        } else if (fVolume > floatVolumeMax) {
            fVolume = floatVolumeMax
        }
        mediaPlayer.setVolume(fVolume, fVolume)
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(progressRunnable)
        handler.removeCallbacksAndMessages(null)
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.release()
        removeAudioFocus()
    }
}