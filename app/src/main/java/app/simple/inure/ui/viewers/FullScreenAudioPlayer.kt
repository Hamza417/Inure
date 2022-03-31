package app.simple.inure.ui.viewers

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.*
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.AudioCoverUtil.loadFromFileDescriptorFullScreen
import app.simple.inure.glide.util.AudioCoverUtil.loadFromFileDescriptorGreyscale
import app.simple.inure.services.AudioService
import app.simple.inure.util.FileUtils.getMimeType
import app.simple.inure.util.NumberUtils
import app.simple.inure.util.StatusBarHeight

class FullScreenAudioPlayer : ScopedFragment() {

    private lateinit var art: ImageView
    private lateinit var artGs: ImageView
    private lateinit var playPause: DynamicRippleImageButton
    private lateinit var close: DynamicRippleImageButton
    private lateinit var duration: TypeFaceTextView
    private lateinit var progress: TypeFaceTextView
    private lateinit var title: TypeFaceTextView
    private lateinit var artist: TypeFaceTextView
    private lateinit var album: TypeFaceTextView
    private lateinit var fileInfo: TypeFaceTextView
    private lateinit var playerContainer: FrameLayout
    private lateinit var seekBar: ThemeSeekBar

    private var animation: ObjectAnimator? = null
    private var uri: Uri? = null
    private var audioService: AudioService? = null
    private var serviceConnection: ServiceConnection? = null
    private var audioBroadcastReceiver: BroadcastReceiver? = null

    private val audioIntentFilter = IntentFilter()
    private var serviceBound = false

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
        val view = inflater.inflate(R.layout.fragment_audio_player, container, false)

        art = view.findViewById(R.id.album_art_mime)
        artGs = view.findViewById(R.id.album_art_gs_mime)
        playPause = view.findViewById(R.id.mime_play_button)
        close = view.findViewById(R.id.mime_close_button)
        duration = view.findViewById(R.id.current_duration_mime)
        progress = view.findViewById(R.id.current_time_mime)
        fileInfo = view.findViewById(R.id.mime_info)
        title = view.findViewById(R.id.mime_title)
        artist = view.findViewById(R.id.mime_artist)
        album = view.findViewById(R.id.mime_album)
        seekBar = view.findViewById(R.id.seekbar_mime)
        playerContainer = view.findViewById(R.id.container)

        uri = requireArguments().getParcelable("uri")!!
        audioIntentFilter.addAction(ServiceConstants.actionPrepared)
        audioIntentFilter.addAction(ServiceConstants.actionQuitService)
        audioIntentFilter.addAction(ServiceConstants.actionMetaData)
        audioIntentFilter.addAction(ServiceConstants.actionPause)
        audioIntentFilter.addAction(ServiceConstants.actionPlay)

        with(playerContainer) {
            setPadding(paddingLeft,
                       StatusBarHeight.getStatusBarHeight(resources) + paddingTop,
                       paddingRight,
                       paddingBottom)
        }

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                if (uri?.getMimeType(requireContext())?.contains("audio")!! || uri?.getMimeType(requireContext())?.contains("video")!!) {
                    serviceBound = true
                    audioService = (service as AudioService.AudioBinder).getService()
                    audioService?.audioUri = uri
                } else {
                    kotlin.runCatching {
                        throw IllegalArgumentException("File is not media type or incompatible")
                    }.getOrElse {
                        val e = Error.newInstance(it.stackTraceToString())
                        e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                            override fun onDismiss() {
                                requireActivity().onBackPressed()
                            }
                        })
                        e.show(childFragmentManager, "error")
                    }
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
                        art.loadFromFileDescriptorFullScreen(uri!!)
                        artGs.loadFromFileDescriptorGreyscale(uri!!)
                    }
                    ServiceConstants.actionQuitService -> {
                        requireContext().unbindService(serviceConnection!!)
                        requireActivity().finishAfterTransition()
                    }
                    ServiceConstants.actionPlay -> {
                        art.animate()
                            .alpha(1F)
                            .setDuration(2500L)
                            .setInterpolator(DecelerateInterpolator(1.5F))
                            .start()
                        buttonStatus(true)
                    }
                    ServiceConstants.actionPause -> {
                        art.animate()
                            .alpha(0F)
                            .setDuration(2500L)
                            .setInterpolator(DecelerateInterpolator(1.5F))
                            .start()
                        buttonStatus(false)
                    }
                    ServiceConstants.actionMediaError -> {
                        val e = Error.newInstance(intent.extras?.getString("stringExtra", "unknown_media_playback_error")!!)
                        e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                            override fun onDismiss() {
                                stopService()
                            }
                        })
                        e.show(childFragmentManager, "error")
                    }
                }
            }
        }

        art.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    art.animate()
                        .alpha(0F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .start()
                }
                MotionEvent.ACTION_UP -> {
                    art.animate()
                        .alpha(1F)
                        .setInterpolator(DecelerateInterpolator(1.5F))
                        .start()

                    kotlin.runCatching {
                        (art.drawable as AnimatedVectorDrawable).start()
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
                    this@FullScreenAudioPlayer.progress.text = NumberUtils.getFormattedTime(progress.toLong())
                    currentPosition = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                animation?.cancel()
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

        art.setOnClickListener {
            playPause.callOnClick()
        }

        playerContainer.setOnClickListener {
            audioService?.changePlayerState()!!
        }

        close.setOnClickListener {
            handler.removeCallbacks(progressRunnable)
            stopService()
            requireActivity().onBackPressed()
        }
    }

    private fun setSeekbarProgress(seekbarProgress: Int) {
        animation = ObjectAnimator.ofInt(seekBar, "progress", seekbarProgress)
        animation!!.duration = 500L
        animation!!.interpolator = LinearOutSlowInInterpolator()
        animation!!.start()
    }

    private fun buttonStatus(isPlaying: Boolean) {
        if (isPlaying) {
            playPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_pause, requireContext().theme))
        } else {
            playPause.setImageDrawable(ResourcesCompat.getDrawable(resources, R.drawable.ic_play, requireContext().theme))
        }
    }

    private fun stopService() {
        serviceBound = false
        requireContext().unbindService(serviceConnection!!)
        requireContext().stopService(Intent(requireContext(), AudioService::class.java))
    }

    private val progressRunnable: Runnable = object : Runnable {
        override fun run() {
            currentPosition = audioService?.getProgress()!!
            setSeekbarProgress(currentPosition)
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

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(audioBroadcastReceiver!!)
    }

    companion object {
        fun newInstance(uri: Uri): FullScreenAudioPlayer {
            val args = Bundle()
            args.putParcelable("uri", uri)
            val fragment = FullScreenAudioPlayer()
            fragment.arguments = args
            return fragment
        }
    }
}