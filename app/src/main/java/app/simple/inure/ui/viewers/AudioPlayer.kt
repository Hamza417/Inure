package app.simple.inure.ui.viewers

import android.annotation.SuppressLint
import android.content.*
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeMaterialCardView
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.extensions.fragments.ScopedAudioPlayerDialogFragment
import app.simple.inure.glide.util.AudioCoverUtil.loadFromFileDescriptor
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.services.AudioService
import app.simple.inure.util.FileUtils.getMimeType
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NumberUtils
import app.simple.inure.util.ViewUtils
import app.simple.inure.util.ViewUtils.gone

class AudioPlayer : ScopedAudioPlayerDialogFragment() {

    private lateinit var art: ImageView
    private lateinit var playPause: DynamicRippleImageButton
    private lateinit var close: DynamicRippleImageButton
    private lateinit var duration: TypeFaceTextView
    private lateinit var progress: TypeFaceTextView
    private lateinit var title: TypeFaceTextView
    private lateinit var artist: TypeFaceTextView
    private lateinit var album: TypeFaceTextView
    private lateinit var fileInfo: TypeFaceTextView
    private lateinit var playerContainer: ThemeMaterialCardView
    private lateinit var seekBar: ThemeSeekBar
    private lateinit var loader: CustomProgressBar

    private var uri: Uri? = null
    private var audioService: AudioService? = null
    private var serviceConnection: ServiceConnection? = null
    private var audioBroadcastReceiver: BroadcastReceiver? = null

    private val audioIntentFilter = IntentFilter()
    private var serviceBound = false
    private var wasSongPlaying = false

    /**
     * [currentPosition] will keep the current position of the playback
     * in the memory. This is necessary in cases where multiple instances of
     * the [app.simple.inure.activities.association.AudioPlayerActivity] is
     * started and the service lost the state of the previous playback so when
     * the instance is resumed let's say from task manager we can easily seek to
     * that position where we left right when onPrepared is called before running
     * our handler [progressRunnable].
     */
    private var currentPosition = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_audio_player, container, false)

        art = view.findViewById(R.id.album_art_mime)
        playPause = view.findViewById(R.id.mime_play_button)
        close = view.findViewById(R.id.mime_close_button)
        duration = view.findViewById(R.id.current_duration_mime)
        progress = view.findViewById(R.id.current_time_mime)
        fileInfo = view.findViewById(R.id.mime_info)
        title = view.findViewById(R.id.mime_title)
        artist = view.findViewById(R.id.mime_artist)
        album = view.findViewById(R.id.mime_album)
        seekBar = view.findViewById(R.id.seekbar_mime)
        loader = view.findViewById(R.id.loader)
        playerContainer = view.findViewById(R.id.container)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            uri = requireArguments().getParcelable("uri", Uri::class.java)!!
        } else {
            @Suppress("DEPRECATION")
            uri = requireArguments().getParcelable("uri")!!
        }

        audioIntentFilter.addAction(ServiceConstants.actionPrepared)
        audioIntentFilter.addAction(ServiceConstants.actionQuitMusicService)
        audioIntentFilter.addAction(ServiceConstants.actionMetaData)
        audioIntentFilter.addAction(ServiceConstants.actionPause)
        audioIntentFilter.addAction(ServiceConstants.actionPlay)
        audioIntentFilter.addAction(ServiceConstants.actionBuffering)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        playerContainer.radius = AppearancePreferences.getCornerRadius()
        ViewUtils.addShadow(playerContainer)

        playerContainer.isEnabled = false
        playPause.isEnabled = false

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                kotlin.runCatching {
                    if ((uri?.getMimeType(requireContext())?.startsWith("audio") == true
                                || uri?.getMimeType(requireContext())?.startsWith("video") == true)
                        || uri?.toString()?.startsWith("http") == true
                        || uri?.toString()?.startsWith("ftp") == true) {
                        serviceBound = true
                        audioService = (service as AudioService.AudioBinder).getService()
                        audioService?.audioUri = uri
                    } else {
                        throw IllegalArgumentException("File is not media type or incompatible")
                    }
                }.getOrElse {
                    it.printStackTrace()
                    showError(it.stackTraceToString())
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }

        audioBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ServiceConstants.actionPrepared -> {
                        audioService?.seek(currentPosition)
                    }
                    ServiceConstants.actionMetaData -> {
                        seekBar.max = audioService?.getDuration()!!
                        duration.text = NumberUtils.getFormattedTime(audioService?.getDuration()?.toLong()!!)
                        handler.post(progressRunnable)
                        title.text = audioService?.metaData?.title
                        artist.text = audioService?.metaData?.artists
                        album.text = audioService?.metaData?.album
                        fileInfo.text = getString(R.string.audio_file_info, audioService?.metaData?.format, audioService?.metaData?.sampling, audioService?.metaData?.bitrate)
                        art.loadFromFileDescriptor(uri!!)
                        loader.gone(animate = true)
                        playerContainer.isEnabled = true
                        playPause.isEnabled = true
                        wasSongPlaying = true
                    }
                    ServiceConstants.actionQuitMusicService -> {
                        if (wasSongPlaying) {
                            requireActivity().finish()
                        } else {
                            kotlin.runCatching {
                                throw IllegalStateException("Service closed unexpectedly on uri: ${uri.toString()}")
                            }.getOrElse {
                                it.printStackTrace()
                                showError(it.stackTraceToString())
                            }
                        }
                    }
                    ServiceConstants.actionPlay -> {
                        buttonStatus(true)
                    }
                    ServiceConstants.actionPause -> {
                        buttonStatus(false)
                    }
                    ServiceConstants.actionBuffering -> {
                        seekBar.updateSecondaryProgress(intent.extras?.getInt(IntentHelper.INT_EXTRA)!!)
                    }
                    ServiceConstants.actionMediaError -> {
                        childFragmentManager.showError(intent.extras?.getString("stringExtra", "unknown_media_playback_error")!!).setOnErrorCallbackListener {
                            stopService()
                        }
                    }
                }
            }
        }

        art.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    art.animate()
                        .scaleX(0.8F)
                        .scaleY(0.8F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    art.animate()
                        .scaleX(1.0F)
                        .scaleY(1.0F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .start()

                    kotlin.runCatching {
                        if (art.drawable is AnimatedVectorDrawable) {
                            (art.drawable as AnimatedVectorDrawable).start()
                        }
                    }.getOrElse {
                        it.printStackTrace()
                    }
                }
            }

            true
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    this@AudioPlayer.progress.text = NumberUtils.getFormattedTime(progress.toLong())
                    currentPosition = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                this@AudioPlayer.seekBar.clearAnimation()
                handler.removeCallbacks(progressRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                audioService?.seek(seekBar.progress)
                handler.post(progressRunnable)
            }
        })

        playPause.setOnClickListener {
            audioService?.changePlayerState()!!
        }

        playerContainer.setOnClickListener {
            audioService?.changePlayerState()!!
        }

        close.setOnClickListener {
            handler.removeCallbacks(progressRunnable)
            stopService()
            dismiss()
        }
    }

    private fun buttonStatus(isPlaying: Boolean) {
        if (isPlaying) {
            playPause.setIcon(R.drawable.ic_pause, true)
        } else {
            playPause.setIcon(R.drawable.ic_play, true)
        }
    }

    private fun stopService() {
        serviceBound = false
        requireContext().unbindService(serviceConnection!!)
        requireContext().stopService(Intent(requireContext(), AudioService::class.java))
        requireActivity().finish()
    }

    private val progressRunnable: Runnable = object : Runnable {
        override fun run() {
            currentPosition = audioService?.getProgress()!!
            seekBar.updateProgress(currentPosition)
            progress.text = NumberUtils.getFormattedTime(currentPosition.toLong())
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onStart() {
        super.onStart()
        val intent = Intent(requireActivity(), AudioService::class.java)
        requireContext().startService(intent)
        serviceConnection?.let { requireContext().bindService(intent, it, Context.BIND_AUTO_CREATE) }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(audioBroadcastReceiver!!, audioIntentFilter)
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacks(progressRunnable)
        if (serviceBound) {
            try {
                serviceConnection?.let { requireContext().unbindService(it) }
            } catch (e: IllegalArgumentException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(audioBroadcastReceiver!!)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        requireActivity().finish()
    }

    companion object {
        fun newInstance(uri: Uri): AudioPlayer {
            val args = Bundle()
            args.putParcelable("uri", uri)
            val fragment = AudioPlayer()
            fragment.arguments = args
            return fragment
        }
    }
}