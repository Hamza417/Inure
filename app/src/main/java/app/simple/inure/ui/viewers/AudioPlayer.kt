package app.simple.inure.ui.viewers

import android.annotation.SuppressLint
import android.content.*
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.R
import app.simple.inure.adapters.music.AlbumArtAdapter
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.services.AudioService
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NumberUtils
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.panels.MusicViewModel
import kotlinx.coroutines.launch

class AudioPlayer : ScopedFragment() {

    private lateinit var artPager: ViewPager2
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

    private var audioModels: ArrayList<AudioModel>? = null
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
     * [currentSeekPosition] will keep the current position of the playback
     * in the memory. This is necessary in cases where multiple instances of
     * the [app.simple.inure.activities.association.AudioPlayerActivity] is
     * started and the service lost the state of the previous playback so when
     * the instance is resumed let's say from task manager we can easily seek to
     * that position where we left right when onPrepared is called before running
     * our handler [progressRunnable].
     */
    private var currentSeekPosition = 0

    private val musicViewModel: MusicViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_audio_player, container, false)

        artPager = view.findViewById(R.id.album_art_mime)
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

        audioModel = requireArguments().parcelable(BundleConstants.audioModel)
        fromActivity = requireArguments().getBoolean(BundleConstants.fromActivity, false)

        audioIntentFilter.addAction(ServiceConstants.actionPrepared)
        audioIntentFilter.addAction(ServiceConstants.actionQuitMusicService)
        audioIntentFilter.addAction(ServiceConstants.actionMetaData)
        audioIntentFilter.addAction(ServiceConstants.actionPause)
        audioIntentFilter.addAction(ServiceConstants.actionPlay)
        audioIntentFilter.addAction(ServiceConstants.actionBuffering)
        audioIntentFilter.addAction(ServiceConstants.actionNext)
        audioIntentFilter.addAction(ServiceConstants.actionPrevious)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        if (fromActivity) {
            startPostponedEnterTransition()
        } else {
            musicViewModel.getSongs().observe(viewLifecycleOwner) {
                audioModels = it
                artPager.adapter = AlbumArtAdapter(audioModels!!)
                artPager.setCurrentItem(requireArguments().getInt(BundleConstants.position, 0), false)

                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }

                artPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        if (state == ViewPager2.SCROLL_STATE_IDLE) {
                            currentSeekPosition = 0
                            audioService?.setCurrentPosition(artPager.currentItem)
                            MusicPreferences.setLastMusicId(audioModels!![artPager.currentItem].id)
                        }
                    }
                })

                lifecycleScope.launch {
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        startService()
                    }
                }
            }
        }

        replayButtonStatus(animate = false)
        playPause.isEnabled = false

        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                serviceBound = true
                audioService = (service as AudioService.AudioBinder).getService()
                audioService?.setAudioPlayerProps(audioModels!!, artPager.currentItem)
                audioService?.setCurrentPosition(artPager.currentItem)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }

        audioBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ServiceConstants.actionPrepared -> {
                        audioService?.seek(currentSeekPosition)
                    }
                    ServiceConstants.actionMetaData -> {
                        try {
                            seekBar.max = audioService?.getDuration()!!
                            duration.text = NumberUtils.getFormattedTime(audioService?.getDuration()?.toLong()!!)
                            handler.post(progressRunnable)
                            title.text = audioService?.metaData?.title
                            artist.text = audioService?.metaData?.artists
                            album.text = audioService?.metaData?.album
                            // fileInfo.text = getString(R.string.audio_file_info, audioService?.metaData?.format, audioService?.metaData?.sampling, audioService?.metaData?.bitrate)
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
                    ServiceConstants.actionNext -> {
                        currentSeekPosition = 0
                        artPager.setCurrentItem(artPager.currentItem + 1, true)
                    }
                    ServiceConstants.actionPrevious -> {
                        currentSeekPosition = 0
                        artPager.setCurrentItem(artPager.currentItem - 1, true)
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
                    currentSeekPosition = progress
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

        close.setOnClickListener {
            handler.removeCallbacks(progressRunnable)
            stopService()
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
            lifecycleScope.launchWhenResumed {
                popBackStack()
            }
        }
    }

    private val progressRunnable: Runnable = object : Runnable {
        override fun run() {
            currentSeekPosition = audioService?.getProgress()!!
            seekBar.updateProgress(currentSeekPosition)
            progress.text = NumberUtils.getFormattedTime(currentSeekPosition.toLong())
            handler.postDelayed(this, 1000L)
        }
    }

    private fun startService() {
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MusicPreferences.musicRepeat -> {
                replayButtonStatus(animate = true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(audioBroadcastReceiver!!)
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

        fun newInstance(position: Int, fromActivity: Boolean = false): AudioPlayer {
            val args = Bundle()
            args.putBoolean(BundleConstants.fromActivity, fromActivity)
            args.putInt(BundleConstants.position, position)
            val fragment = AudioPlayer()
            fragment.arguments = args
            return fragment
        }
    }
}