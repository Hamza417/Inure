package app.simple.inure.services

import android.app.*
import android.content.*
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import app.simple.inure.R
import app.simple.inure.activities.association.AudioPlayerActivity
import app.simple.inure.activities.association.FullScreenAudioPlayerActivity
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.exceptions.InureMediaEngineException
import app.simple.inure.models.AudioMetaData
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.receivers.MediaButtonIntentReceiver
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.MetadataHelper
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.NullSafety.isNull
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.math.ln

class AudioService : Service(),
                     AudioManager.OnAudioFocusChangeListener,
                     MediaPlayer.OnCompletionListener,
                     MediaPlayer.OnPreparedListener,
                     MediaPlayer.OnErrorListener,
                     MediaPlayer.OnSeekCompleteListener,
                     SharedPreferences.OnSharedPreferenceChangeListener {

    private val binder = AudioBinder()
    private val mediaPlayer = MediaPlayer()
    private val audioBecomingNoisyFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)

    private var mediaSessionCompat: MediaSessionCompat? = null
    private var audioManager: AudioManager? = null
    private var focusRequest: AudioFocusRequest? = null
    private var notificationManager: NotificationManager? = null
    private var builder: NotificationCompat.Builder? = null
    private var timer: Timer? = null
    private var timerTask: TimerTask? = null

    private val volumeFadeDuration: Int = 250
    private var iVolume = 0
    private val intVolumeMax = 100
    private val intVolumeMin = 0
    private val floatVolumeMax = 1f
    private val floatVolumeMin = 0f
    private val channelId = "inure_mini_player"
    private val notificationId = 54786214

    private var wasPlaying = false

    var metaData: AudioMetaData? = null

    var audioUri: Uri? = null
        set(value) {
            if (field.isNull() || field != value) {
                field = value
                audioPlayer(value!!)
            } else if (field == value) {
                setupMetadata()
            }
        }

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            pause()
        }
    }

    inner class AudioBinder : Binder() {
        fun getService(): AudioService {
            return this@AudioService
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent.isNotNull()) {
            MediaButtonIntentReceiver.handleIntent(baseContext, intent!!)

            when (intent.action) {
                ServiceConstants.actionPlay -> {
                    play()
                }
                ServiceConstants.actionPause -> {
                    pause()
                }
                ServiceConstants.actionTogglePause -> {
                    changePlayerState()
                }
                ServiceConstants.actionQuitService -> {
                    stopForeground(true)
                    stopSelf()
                    IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionQuitService, applicationContext)
                }
            }
        }
        return START_REDELIVER_INTENT
    }

    override fun onCreate() {
        super.onCreate()

        app.simple.inure.preferences.SharedPreferences.init(applicationContext)
        app.simple.inure.preferences.SharedPreferences.getSharedPreferences().registerOnSharedPreferenceChangeListener(this)

        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)
                .setAudioAttributes(AudioAttributes.Builder()
                                        .setUsage(AudioAttributes.USAGE_MEDIA)
                                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                .setWillPauseWhenDucked(true)
                .setOnAudioFocusChangeListener(this)
                .build()
        }

        registerReceiver(becomingNoisyReceiver, audioBecomingNoisyFilter)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                if (!mediaPlayer.isPlaying) {
                    if (wasPlaying) {
                        play()
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
                    pause()
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
        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionQuitService, applicationContext)
        stopForeground(true)
    }

    override fun onPrepared(mp: MediaPlayer?) {
        if (requestAudioFocus()) {
            mp?.start()
            IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionPrepared, applicationContext)
            setupMetadata()
        }
    }

    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        kotlin.runCatching {
            when (what) {
                MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> {
                    throw InureMediaEngineException("MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK & extra ${ServiceConstants.getMediaErrorString(extra)}")
                }
                MediaPlayer.MEDIA_ERROR_SERVER_DIED -> {
                    throw InureMediaEngineException("MEDIA_ERROR_SERVER_DIED & extra ${ServiceConstants.getMediaErrorString(extra)}")
                }
                MediaPlayer.MEDIA_ERROR_UNKNOWN -> {
                    throw InureMediaEngineException("MEDIA_ERROR_UNKNOWN & extra ${ServiceConstants.getMediaErrorString(extra)}")
                }
                else -> {
                    /* no-op */
                }
            }
        }.onFailure {
            IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionMediaError, context = applicationContext, it.stackTraceToString())
        }

        return true
    }

    override fun onSeekComplete(mp: MediaPlayer?) {
        /* no-op */
    }

    private fun setupMediaSession() {
        val mediaButtonReceiverComponentName = ComponentName(applicationContext, MediaButtonIntentReceiver::class.java)
        val mediaButtonIntent = Intent(Intent.ACTION_MEDIA_BUTTON)
        mediaButtonIntent.component = mediaButtonReceiverComponentName
        val mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(applicationContext, 110, mediaButtonIntent, PendingIntent.FLAG_IMMUTABLE)
        mediaSessionCompat = MediaSessionCompat(this, getString(R.string.mini_player_name), mediaButtonReceiverComponentName, mediaButtonReceiverPendingIntent)
        mediaSessionCompat!!.setCallback(object : MediaSessionCompat.Callback() {
            override fun onPlay() {
                play()
            }

            override fun onPause() {
                pause()
            }

            override fun onSkipToNext() {
                /* no-op */
            }

            override fun onSkipToPrevious() {
                /* no-op */
            }

            override fun onStop() {
                stopForeground(true)
                stopSelf()
            }

            override fun onSeekTo(pos: Long) {
                seek(pos.toInt())
            }

            override fun onMediaButtonEvent(mediaButtonEvent: Intent): Boolean {
                return MediaButtonIntentReceiver.handleIntent(this@AudioService, mediaButtonEvent)
            }
        })

        @Suppress("deprecation")
        mediaSessionCompat!!.setFlags(MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS or MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS)
        mediaSessionCompat!!.setMediaButtonReceiver(mediaButtonReceiverPendingIntent)
        mediaSessionCompat!!.isActive = true
        //mediaControllerCompat = mediaSessionCompat.controller
        //mediaMetadataCompat = mediaControllerCompat.metadata
    }

    private fun setPlaybackState(playbackState: Int) {
        mediaSessionCompat?.setPlaybackState(
                PlaybackStateCompat.Builder()
                    .setState(playbackState, mediaPlayer.currentPosition.toLong(), 1f)
                    .setActions(PlaybackStateCompat.ACTION_SEEK_TO)
                    .build()
        )
    }

    private fun setupMetadata() {
        CoroutineScope(Dispatchers.IO).launch {
            kotlin.runCatching {
                metaData = MetadataHelper.getAudioMetadata(applicationContext, audioUri!!)

                val mediaMetadata = MediaMetadataCompat.Builder()
                    .putString(MediaMetadataCompat.METADATA_KEY_TITLE, metaData?.title)
                    .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, metaData?.artists)
                    .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, metaData?.album)
                    // .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, metaData?.artUri)
                    .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mediaPlayer.duration.toLong())
                    .build()

                withContext(Dispatchers.Main) {
                    IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionMetaData, applicationContext)
                    setupMediaSession()
                    createNotificationChannel()
                    showNotification(generateAction(R.drawable.ic_pause, "pause", ServiceConstants.actionPause))
                    mediaSessionCompat?.setMetadata(mediaMetadata)
                    setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                }
            }.getOrElse {
                withContext(Dispatchers.Main) {
                    IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionMediaError, applicationContext, it.stackTraceToString())
                }
            }
        }
    }

    private fun requestAudioFocus(): Boolean {
        val value: Int
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            value = audioManager?.requestAudioFocus(focusRequest!!)!!
        } else {
            @Suppress("deprecation") // Required for older APIs
            value = audioManager?.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)!!
        }

        return value == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    private fun removeAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager?.abandonAudioFocusRequest(focusRequest!!)
        } else {
            @Suppress("Deprecation")
            audioManager?.abandonAudioFocus(this)
        }
    }

    private fun audioPlayer(uri: Uri) {
        mediaPlayer.reset()
        mediaPlayer.setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
        mediaPlayer.setAudioAttributes(AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
        mediaPlayer.setOnCompletionListener(this)
        mediaPlayer.setOnPreparedListener(this)
        mediaPlayer.setOnSeekCompleteListener(this)
        mediaPlayer.setDataSource(applicationContext, uri)
        mediaPlayer.prepareAsync()
    }

    internal fun getProgress(): Int {
        return mediaPlayer.currentPosition
    }

    internal fun getDuration(): Int {
        return mediaPlayer.duration
    }

    internal fun seek(to: Int) {
        mediaPlayer.seekTo(to)
        setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
    }

    internal fun changePlayerState(): Boolean {
        if (mediaPlayer.isPlaying) {
            pause()
        } else {
            play()
        }

        return mediaPlayer.isPlaying
    }

    private fun pause() {
        IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionPause, applicationContext)

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
                            setPlaybackState(PlaybackStateCompat.STATE_PAUSED)
                            showNotification(generateAction(R.drawable.ic_play, "play", ServiceConstants.actionPlay))
                            stopForeground(false)
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
                setPlaybackState(PlaybackStateCompat.STATE_PLAYING)
                showNotification(generateAction(R.drawable.ic_pause, "pause", ServiceConstants.actionPause))
                IntentHelper.sendLocalBroadcastIntent(ServiceConstants.actionPlay, applicationContext)
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

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name: CharSequence = getString(R.string.audio_player)
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(action: NotificationCompat.Action) {
        notificationManager = baseContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        val intentAction = if (DevelopmentPreferences.isAudioPlayerFullScreen()) {
            Intent(this, FullScreenAudioPlayerActivity::class.java)
        } else {
            Intent(this, AudioPlayerActivity::class.java)
        }

        intentAction.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        intentAction.data = audioUri

        val buttonClick = PendingIntent.getActivity(this, 111, intentAction, PendingIntent.FLAG_UPDATE_CURRENT)

        builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_audio_placeholder)
            .setLargeIcon(metaData?.art)
            .addAction(action) /* Play Pause Action */
            .addAction(generateAction(R.drawable.ic_close, "Close", ServiceConstants.actionQuitService))
            .setContentTitle(metaData?.title)
            .setContentText(metaData?.artists)
            .setSubText(metaData?.artists)
            .setContentIntent(buttonClick)
            .setShowWhen(false)
            .setColorized(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            // Setting this style will not show notification icon in some devices
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSessionCompat!!.sessionToken))
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

        val notification: Notification = builder!!.build()
        notificationManager!!.notify(notificationId, notification)
        startForeground(notificationId, notification)
    }

    private fun generateAction(icon: Int, title: String, action: String): NotificationCompat.Action {
        val intent = Intent(this, AudioService::class.java)
        intent.action = action
        val close = PendingIntent.getService(this, 5087847, intent, PendingIntent.FLAG_IMMUTABLE)
        return NotificationCompat.Action.Builder(icon, title, close).build()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            DevelopmentPreferences.isAudioPlayerFullScreen -> {
                if (mediaPlayer.isPlaying) {
                    showNotification(generateAction(R.drawable.ic_pause, "pause", ServiceConstants.actionPause))
                } else {
                    showNotification(generateAction(R.drawable.ic_play, "play", ServiceConstants.actionPlay))
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.stop()
        mediaPlayer.reset()
        mediaPlayer.release()
        removeAudioFocus()
        unregisterReceiver(becomingNoisyReceiver)
        app.simple.inure.preferences.SharedPreferences.getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
    }
}