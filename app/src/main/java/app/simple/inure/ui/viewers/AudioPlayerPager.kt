package app.simple.inure.ui.viewers

import android.animation.Animator
import android.annotation.SuppressLint
import android.content.*
import android.content.SharedPreferences
import android.os.Bundle
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.SeekBar
import androidx.core.app.SharedElementCallback
import androidx.core.view.doOnPreDraw
import androidx.core.view.get
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.R
import app.simple.inure.adapters.music.AlbumArtAdapter
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.lrc.LrcHelper
import app.simple.inure.decorations.lrc.LrcView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.miscellaneous.Error.Companion.showError
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.models.AudioModel
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.services.AudioServicePager
import app.simple.inure.util.AudioUtils.toBitrate
import app.simple.inure.util.IntentHelper
import app.simple.inure.util.NumberUtils
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.panels.MusicViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class AudioPlayerPager : ScopedFragment() {

    private lateinit var artPager: ViewPager2
    private lateinit var lrcView: LrcView
    private lateinit var replay: DynamicRippleImageButton
    private lateinit var playPause: DynamicRippleImageButton
    private lateinit var close: DynamicRippleImageButton
    private lateinit var next: DynamicRippleImageButton
    private lateinit var previous: DynamicRippleImageButton
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
    private var audioServicePager: AudioServicePager? = null
    private var serviceConnection: ServiceConnection? = null
    private var audioBroadcastReceiver: BroadcastReceiver? = null

    private val audioIntentFilter = IntentFilter()
    private var serviceBound = false
    private var wasSongPlaying = false
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
        val view = inflater.inflate(R.layout.fragment_audio_player_pager, container, false)

        artPager = view.findViewById(R.id.album_art_mime)
        lrcView = view.findViewById(R.id.lrc_view)
        replay = view.findViewById(R.id.mime_repeat_button)
        playPause = view.findViewById(R.id.mime_play_button)
        close = view.findViewById(R.id.mime_close_button)
        next = view.findViewById(R.id.mime_next_button)
        previous = view.findViewById(R.id.mime_previous_button)
        duration = view.findViewById(R.id.current_duration_mime)
        progress = view.findViewById(R.id.current_time_mime)
        fileInfo = view.findViewById(R.id.mime_info)
        title = view.findViewById(R.id.mime_title)
        artist = view.findViewById(R.id.mime_artist)
        album = view.findViewById(R.id.mime_album)
        seekBar = view.findViewById(R.id.seekbar_mime)
        loader = view.findViewById(R.id.loader)

        audioModel = requireArguments().parcelable(BundleConstants.audioModel)

        audioIntentFilter.addAction(ServiceConstants.actionPreparedPager)
        audioIntentFilter.addAction(ServiceConstants.actionQuitMusicServicePager)
        audioIntentFilter.addAction(ServiceConstants.actionMetaDataPager)
        audioIntentFilter.addAction(ServiceConstants.actionPausePager)
        audioIntentFilter.addAction(ServiceConstants.actionPlayPager)
        audioIntentFilter.addAction(ServiceConstants.actionBufferingPager)
        audioIntentFilter.addAction(ServiceConstants.actionNextPager)
        audioIntentFilter.addAction(ServiceConstants.actionPreviousPager)

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        if (requireArguments().getBoolean(BundleConstants.fromSearch)) {
            musicViewModel.getSearched().observe(viewLifecycleOwner) {
                audioModels = it
                artPager.adapter = AlbumArtAdapter(audioModels!!)
                artPager.setCurrentItem(requireArguments().getInt(BundleConstants.position, 0), false)

                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }

                setLrc()
                setMetaData(artPager.currentItem)

                artPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        if (state == ViewPager2.SCROLL_STATE_IDLE) {
                            currentSeekPosition = 0
                            MusicPreferences.setMusicPosition(artPager.currentItem)
                            audioServicePager?.setCurrentPosition(artPager.currentItem)
                            MusicPreferences.setLastMusicId(audioModels!![artPager.currentItem].id)
                            requireArguments().putInt(BundleConstants.position, artPager.currentItem)
                            setMetaData(artPager.currentItem)
                            setLrc()
                        }
                    }
                })

                lifecycleScope.launch { // OnStart, but on steroids!!!
                    repeatOnLifecycle(Lifecycle.State.STARTED) {
                        startService()
                    }
                }
            }
        } else {
            musicViewModel.getSongs().observe(viewLifecycleOwner) {
                audioModels = it
                artPager.adapter = AlbumArtAdapter(audioModels!!)
                artPager.setCurrentItem(requireArguments().getInt(BundleConstants.position, 0), false)

                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }

                setLrc()
                setMetaData(artPager.currentItem)

                artPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrollStateChanged(state: Int) {
                        super.onPageScrollStateChanged(state)
                        if (state == ViewPager2.SCROLL_STATE_IDLE) {
                            if (artPager.currentItem != MusicPreferences.getMusicPosition()) {
                                currentSeekPosition = 0
                                MusicPreferences.setMusicPosition(artPager.currentItem)
                                audioServicePager?.setCurrentPosition(artPager.currentItem)
                                MusicPreferences.setLastMusicId(audioModels!![artPager.currentItem].id)
                                requireArguments().putInt(BundleConstants.position, artPager.currentItem)
                                setMetaData(artPager.currentItem)
                                setLrc()
                            }
                        }
                    }
                })

                lifecycleScope.launch { // OnStart, but on steroids!!!
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
                audioServicePager = (service as AudioServicePager.AudioBinder).getService()
                audioServicePager?.setAudioPlayerProps(audioModels!!, artPager.currentItem)
                audioServicePager?.setCurrentPosition(artPager.currentItem)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                serviceBound = false
            }
        }

        audioBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    ServiceConstants.actionPreparedPager -> {
                        audioServicePager?.seek(currentSeekPosition)
                    }
                    ServiceConstants.actionMetaDataPager -> {
                        try {
                            seekBar.max = audioServicePager?.getDuration()!!
                            duration.text = NumberUtils.getFormattedTime(audioServicePager?.getDuration()?.toLong()!!)
                            handler.post(progressRunnable)
                            loader.gone(animate = true)
                            playPause.isEnabled = true
                            wasSongPlaying = true
                            buttonStatus(audioServicePager?.isPlaying()!!, animate = false)
                        } catch (e: IllegalStateException) {
                            e.printStackTrace()
                            showError(e.stackTraceToString())
                        }
                    }
                    ServiceConstants.actionQuitMusicServicePager -> {
                        finish()
                    }
                    ServiceConstants.actionPlayPager -> {
                        buttonStatus(true)
                    }
                    ServiceConstants.actionPausePager -> {
                        buttonStatus(false)
                    }
                    ServiceConstants.actionNextPager -> {
                        currentSeekPosition = 0
                        if (artPager.currentItem < audioModels!!.size - 1) {
                            artPager.setCurrentItem(artPager.currentItem + 1, true)
                        } else {
                            artPager.setCurrentItem(0, true)
                        }

                        setMetaData(artPager.currentItem)
                    }
                    ServiceConstants.actionPreviousPager -> {
                        currentSeekPosition = 0
                        if (artPager.currentItem > 0) {
                            artPager.setCurrentItem(artPager.currentItem - 1, true)
                        } else {
                            artPager.setCurrentItem(audioModels!!.size - 1, true)
                        }

                        setMetaData(artPager.currentItem)
                    }
                    ServiceConstants.actionBufferingPager -> {
                        seekBar.updateSecondaryProgress(intent.extras?.getInt(IntentHelper.INT_EXTRA)!!)
                    }
                    ServiceConstants.actionMediaErrorPager -> {
                        childFragmentManager.showError(intent.extras?.getString("stringExtra", "unknown_media_playback_error")!!).setOnErrorCallbackListener {
                            stopService()
                        }
                    }
                }
            }
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    this@AudioPlayerPager.progress.text = NumberUtils.getFormattedTime(progress.toLong())
                    currentSeekPosition = progress
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
                this@AudioPlayerPager.seekBar.clearAnimation()
                handler.removeCallbacks(progressRunnable)
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                audioServicePager?.seek(seekBar.progress)
                handler.post(progressRunnable)
            }
        })

        replay.setOnClickListener {
            MusicPreferences.setMusicRepeat(!MusicPreferences.getMusicRepeat())
        }

        playPause.setOnClickListener {
            audioServicePager?.changePlayerState()!!
        }

        close.setOnClickListener {
            handler.removeCallbacks(progressRunnable)
            stopService()
        }

        next.setOnClickListener {
            audioServicePager?.playNext()
        }

        previous.setOnClickListener {
            audioServicePager?.playPrevious()
        }

        lrcView.setOnPlayIndicatorLineListener { time, _ ->
            audioServicePager?.seek(time.toInt())
        }

        setEnterSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: List<String?>, sharedElements: MutableMap<String?, View?>) {
                // Locate the ViewHolder for the clicked position.
                val selectedViewHolder = (artPager[0] as RecyclerView).findViewHolderForAdapterPosition(MusicPreferences.getMusicPosition())
                if (selectedViewHolder is AlbumArtAdapter.Holder) {
                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] = selectedViewHolder.itemView.findViewById(R.id.album_art)
                }
            }
        })
    }

    private fun setMetaData(position: Int) {
        title.text = audioModels!![position].title
        artist.text = audioModels!![position].artists
        album.text = audioModels!![position].album
        fileInfo.text = getString(
                R.string.audio_file_info,
                "." + audioModels!![position].path?.substringAfterLast("."),
                audioModels!![position].bitrate.toBitrate(),
                audioModels!![position].mimeType)
    }

    private fun buttonStatus(isPlaying: Boolean, animate: Boolean = true) {
        if (isPlaying) {
            playPause.setIcon(R.drawable.ic_pause, animate)
        } else {
            playPause.setIcon(R.drawable.ic_play, animate)
        }
    }

    private fun setLrc() {
        lifecycleScope.launch {
            with(File(audioModels!![artPager.currentItem].path.replaceAfterLast(".", "lrc"))) {
                if (exists()) {
                    lrcView.setLrcData(LrcHelper.parseLrcFromFile(this))
                    delay(1000)
                    lrcView.animate()
                        .alpha(1F)
                        .setInterpolator(AccelerateInterpolator())
                        .setDuration(resources.getInteger(R.integer.animation_duration).toLong())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                lrcView.visibility = View.VISIBLE
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                /* no-op */
                            }

                            override fun onAnimationCancel(animation: Animator) {
                                /* no-op */
                            }

                            override fun onAnimationRepeat(animation: Animator) {
                                /* no-op */
                            }
                        })
                        .start()
                } else {
                    delay(1000)
                    lrcView.animate()
                        .alpha(0F)
                        .setInterpolator(AccelerateInterpolator())
                        .setDuration(resources.getInteger(R.integer.animation_duration).toLong())
                        .setListener(object : Animator.AnimatorListener {
                            override fun onAnimationStart(animation: Animator) {
                                /* no-op */
                            }

                            override fun onAnimationEnd(animation: Animator) {
                                lrcView.visibility = View.GONE
                            }

                            override fun onAnimationCancel(animation: Animator) {
                                /* no-op */
                            }

                            override fun onAnimationRepeat(animation: Animator) {
                                /* no-op */
                            }
                        })
                        .start()
                }
            }
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
        requireContext().stopService(Intent(requireContext(), AudioServicePager::class.java))
        finish()
    }

    private fun finish() {
        isFinished = true
        lifecycleScope.launchWhenResumed {
            if (requireActivity().supportFragmentManager.backStackEntryCount > 0) {
                popBackStack()
            } else {
                requireActivity().finish()
            }
        }
    }

    private val progressRunnable: Runnable = object : Runnable {
        override fun run() {
            currentSeekPosition = audioServicePager?.getProgress()!!
            seekBar.updateProgress(currentSeekPosition)
            lrcView.updateTime(currentSeekPosition.toLong())
            progress.text = NumberUtils.getFormattedTime(currentSeekPosition.toLong())
            handler.postDelayed(this, 1000L)
        }
    }

    private fun startService() {
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(audioBroadcastReceiver!!) // Just to be safe
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(audioBroadcastReceiver!!, audioIntentFilter)
        val intent = Intent(requireActivity(), AudioServicePager::class.java)
        requireContext().startService(intent)
        serviceConnection?.let { requireContext().bindService(intent, it, Context.BIND_AUTO_CREATE) }
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
        fun newInstance(position: Int, fromSearch: Boolean = false): AudioPlayerPager {
            val args = Bundle()
            MusicPreferences.setFromSearch(fromSearch)
            args.putBoolean(BundleConstants.fromSearch, fromSearch)
            args.putInt(BundleConstants.position, position)
            val fragment = AudioPlayerPager()
            fragment.arguments = args
            return fragment
        }
    }
}