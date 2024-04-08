package app.simple.inure.ui.association

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.glide.filedescriptorcover.DescriptorCoverModel
import app.simple.inure.glide.modules.GlideApp
import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.services.AudioService
import app.simple.inure.util.ActivityUtils.isAppInLockTaskMode
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.FileUtils.getMimeType
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.NumberUtils
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.gone
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import kotlinx.coroutines.launch

class AudioPlayer : ScopedFragment() {

    private lateinit var art: ImageView
    private lateinit var replay: DynamicRippleImageButton
    private lateinit var playPause: DynamicRippleImageButton
    private lateinit var close: DynamicRippleImageButton
    private lateinit var duration: TypeFaceTextView
    private lateinit var progress: TypeFaceTextView
    private lateinit var title: TypeFaceTextView
    private lateinit var artist: TypeFaceTextView
    private lateinit var album: TypeFaceTextView
    private lateinit var fileInfo: TypeFaceTextView
    private lateinit var seekBar: ThemeSeekBar
    private lateinit var loader: CustomProgressBar

    private var uri: Uri? = null
    private var audioModel: AudioModel? = null
    private var audioService: AudioService? = null
    private var serviceConnection: ServiceConnection? = null
    private var audioBroadcastReceiver: BroadcastReceiver? = null

    private val audioIntentFilter = IntentFilter()
    private var serviceBound = false
    private var wasSongPlaying = false
    private var fromActivity = false
    private var isFinished = false

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
        replay = view.findViewById(R.id.mime_repeat_button)
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

        kotlin.runCatching {
            uri = requireArguments().parcelable(BundleConstants.uri)
            if (uri.isNull()) throw NullPointerException("Uri is null")
            art.transitionName = uri.toString()
        }.onFailure {
            // Probably the [AudioModel] mode
            audioModel = requireArguments().parcelable(BundleConstants.audioModel)
            art.transitionName = audioModel?.fileUri.toString()
        }

        fromActivity = requireArguments().getBoolean(BundleConstants.fromActivity, false)

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

        if (fromActivity) {
            /**
             * This will solve the Glide and image size issues
             * after the layout has changed while.
             */
            art.requestLayout()
            art.post {
                art.loadFromFileDescriptor(uri!!)
            }
        } else {
            if (DevelopmentPreferences.get(DevelopmentPreferences.loadAlbumArtFromFile)) {
                /**
                 * This will solve the Glide and image size issues
                 * after the layout has changed while.
                 */
                if (audioModel.isNotNull()) {
                    art.requestLayout()
                    art.post {
                        art.loadFromFileDescriptor(audioModel?.fileUri?.toUri()!!)
                    }

                    title.text = audioModel?.title
                    artist.text = audioModel?.artists
                    album.text = audioModel?.album
                } else {
                    art.requestLayout()
                    art.post {
                        art.loadFromFileDescriptor(uri!!)
                    }
                }
            } else {
                art.scaleType = ImageView.ScaleType.CENTER_CROP
                art.setImageURI(audioModel?.artUri?.toUri())
                startPostponedEnterTransition()

                title.text = audioModel?.title
                artist.text = audioModel?.artists
                album.text = audioModel?.album
            }
        }

        replayButtonStatus(animate = false)
        playPause.isEnabled = false

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                kotlin.runCatching {
                    if (uri.isNull()) {
                        if ((audioModel?.fileUri?.toUri()?.getMimeType(requireContext())?.startsWith("audio") == true
                                        || audioModel?.fileUri?.toUri()?.getMimeType(requireContext())?.startsWith("video") == true)
                                || audioModel?.fileUri?.toUri()?.toString()?.startsWith("http") == true
                                || audioModel?.fileUri?.toUri()?.toString()?.startsWith("ftp") == true) {
                            serviceBound = true
                            audioService = (service as AudioService.AudioBinder).getService()
                            audioService?.audioUri = audioModel?.fileUri?.toUri()
                        } else {
                            throw IllegalArgumentException("File is not media type or incompatible")
                        }
                    } else {
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
                    }
                }.getOrElse {
                    it.printStackTrace()
                    showWarning(it.message.toString())
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
                        try {
                            seekBar.max = audioService?.getDuration()!!
                            duration.text = NumberUtils.getFormattedTime(audioService?.getDuration()?.toLong()!!)
                            handler.post(progressRunnable)
                            if (uri.isNotNull()) {
                                title.text = audioService?.metaData?.title
                                artist.text = audioService?.metaData?.artists
                                album.text = audioService?.metaData?.album
                            }
                            fileInfo.text = getString(R.string.audio_file_info, audioService?.metaData?.format, audioService?.metaData?.sampling, audioService?.metaData?.bitrate)
                            loader.gone(animate = true)
                            playPause.isEnabled = true

                            wasSongPlaying = true
                            buttonStatus(audioService?.isPlaying()!!, animate = false)
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                            showError(e.stackTraceToString())
                        }
                    }
                    ServiceConstants.actionQuitMusicService -> {
                        finish()
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

        //        art.setOnTouchListener { _, event ->
        //            when (event.action) {
        //                MotionEvent.ACTION_DOWN -> {
        //                    art.animate()
        //                        .scaleX(1.2F)
        //                        .scaleY(1.2F)
        //                        .setInterpolator(DecelerateInterpolator(1.5F))
        //                        .start()
        //                }
        //                MotionEvent.ACTION_UP -> {
        //                    art.animate()
        //                        .scaleX(1.0F)
        //                        .scaleY(1.0F)
        //                        .setInterpolator(DecelerateInterpolator(1.5F))
        //                        .start()
        //
        //                    kotlin.runCatching {
        //                        if (art.drawable is AnimatedVectorDrawable) {
        //                            (art.drawable as AnimatedVectorDrawable).start()
        //                        }
        //                    }.getOrElse {
        //                        it.printStackTrace()
        //                    }
        //                }
        //            }
        //
        //            false
        //        }

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

        replay.setOnClickListener {
            MusicPreferences.setMusicRepeat(!MusicPreferences.getMusicRepeat())
        }

        playPause.setOnClickListener {
            audioService?.changePlayerState()!!
        }

        art.setOnClickListener {
            audioService?.changePlayerState()!!

            kotlin.runCatching {
                if (art.drawable is AnimatedVectorDrawable) {
                    (art.drawable as AnimatedVectorDrawable).start()
                }
            }
        }

        close.setOnClickListener {
            if (requireContext().isAppInLockTaskMode().invert()) {
                handler.removeCallbacks(progressRunnable)
                stopService()
            } else {
                showWarning(getString(R.string.lock_task_warning), false)
            }
        }
    }

    private fun buttonStatus(isPlaying: Boolean, animate: Boolean = true) {
        if (isPlaying) {
            playPause.setIcon(R.drawable.ic_pause, animate)
        } else {
            playPause.setIcon(R.drawable.ic_play, animate)
        }
    }

    private fun replayButtonStatus(animate: Boolean = true) {
        if (MusicPreferences.getMusicRepeat()) {
            if (animate) {
                replay.animate().alpha(1.0F).setDuration(resources.getInteger(R.integer.animation_duration).toLong()).start()
            } else {
                replay.alpha = 1.0F
            }
        } else {
            if (animate) {
                replay.animate().alpha(0.3F).setDuration(resources.getInteger(R.integer.animation_duration).toLong()).start()
            } else {
                replay.alpha = 0.3F
            }
        }
    }

    private fun stopService() {
        serviceBound = false
        requireContext().unbindService(serviceConnection!!)
        requireContext().stopService(Intent(requireContext(), AudioService::class.java))
        finish()
    }

    private fun finish() {
        isFinished = true
        if (fromActivity) {
            requireActivity().finish()
        } else {
            lifecycleScope.launch {
                repeatOnLifecycle(Lifecycle.State.RESUMED) {
                    popBackStack()
                }
            }
        }
    }

    private val progressRunnable: Runnable = object : Runnable {
        override fun run() {
            currentPosition = audioService?.getProgress()!!
            seekBar.updateProgress(currentPosition)
            progress.text = NumberUtils.getFormattedTime(currentPosition.toLong())
            handler.postDelayed(this, 1000L)
        }
    }

    private fun startService() {
        val intent = Intent(requireActivity(), AudioService::class.java)
        requireContext().startService(intent)
        serviceConnection?.let { requireContext().bindService(intent, it, Context.BIND_AUTO_CREATE) }
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(audioBroadcastReceiver!!, audioIntentFilter)
    }

    override fun onStart() {
        super.onStart()
        if (isFinished.invert()) {
            startService()
        }
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MusicPreferences.MUSIC_REPEAT -> {
                replayButtonStatus(animate = true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(audioBroadcastReceiver!!)
    }

    /**
     * @param uri requires a valid file uri and not art uri else
     * error 0x80000000 will be thrown by the MediaMetadataRetriever
     *
     * Asynchronously load Album Arts for song files from their URIs using file descriptor
     */
    fun ImageView.loadFromFileDescriptor(uri: Uri) {
        postponeEnterTransition()

        GlideApp.with(this)
            .asBitmap()
            .dontAnimate()
            .transform(CenterCrop())
            .load(DescriptorCoverModel(this.context, uri))
            .addListener(object : RequestListener<Bitmap> {
                override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Bitmap>?, isFirstResource: Boolean): Boolean {
                    this@loadFromFileDescriptor.setImageResource(R.drawable.ani_ic_app_icon).also {
                        (this@loadFromFileDescriptor.drawable as AnimatedVectorDrawable).start()
                    }
                    (view?.parent as? ViewGroup)?.doOnPreDraw {
                        startPostponedEnterTransition()
                    }

                    return true
                }

                override fun onResourceReady(resource: Bitmap?, model: Any?, target: Target<Bitmap>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
                    (view?.parent as? ViewGroup)?.doOnPreDraw {
                        startPostponedEnterTransition()
                    }
                    return false
                }
            })
            .into(this)
    }

    companion object {
        fun newInstance(uri: Uri, fromActivity: Boolean = false): AudioPlayer {
            val args = Bundle()
            args.putParcelable(BundleConstants.uri, uri)
            args.putBoolean(BundleConstants.fromActivity, fromActivity)
            val fragment = AudioPlayer()
            fragment.arguments = args
            return fragment
        }

        fun newInstance(audioModel: AudioModel, fromActivity: Boolean = false): AudioPlayer {
            val args = Bundle()
            args.putParcelable(BundleConstants.audioModel, audioModel)
            args.putBoolean(BundleConstants.fromActivity, fromActivity)
            val fragment = AudioPlayer()
            fragment.arguments = args
            return fragment
        }
    }
}
