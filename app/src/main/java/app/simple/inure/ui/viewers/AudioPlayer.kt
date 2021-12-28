package app.simple.inure.ui.viewers

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.graphics.drawable.AnimatedVectorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.factories.dialog.MediaPlayerViewModelFactory
import app.simple.inure.glide.util.AudioCoverUtil.loadFromFileDescriptor
import app.simple.inure.preferences.AppearancePreferences
import app.simple.inure.util.NumberUtils
import app.simple.inure.util.ViewUtils
import app.simple.inure.viewmodels.dialogs.MediaPlayerViewModel
import com.google.android.material.card.MaterialCardView

class AudioPlayer : ScopedBottomSheetFragment() {

    private lateinit var art: ImageView
    private lateinit var playPause: DynamicRippleImageButton
    private lateinit var duration: TypeFaceTextView
    private lateinit var progress: TypeFaceTextView
    private lateinit var title: TypeFaceTextView
    private lateinit var artist: TypeFaceTextView
    private lateinit var album: TypeFaceTextView
    private lateinit var fileInfo: TypeFaceTextView
    private lateinit var playerContainer: MaterialCardView
    private lateinit var seekBar: SeekBar

    private lateinit var playerViewModel: MediaPlayerViewModel
    private lateinit var mediaPlayerViewModelFactory: MediaPlayerViewModelFactory
    private var animation: ObjectAnimator? = null

    private val durationSmoother = 1000

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_audio_player, container, false)

        art = view.findViewById(R.id.album_art_mime)
        playPause = view.findViewById(R.id.mime_play_button)
        duration = view.findViewById(R.id.current_duration_mime)
        progress = view.findViewById(R.id.current_time_mime)
        fileInfo = view.findViewById(R.id.mime_info)
        title = view.findViewById(R.id.mime_title)
        artist = view.findViewById(R.id.mime_artist)
        album = view.findViewById(R.id.mime_album)
        seekBar = view.findViewById(R.id.seekbar_mime)
        playerContainer = view.findViewById(R.id.container)

        mediaPlayerViewModelFactory = MediaPlayerViewModelFactory(requireActivity().application, requireArguments().getParcelable("uri")!!)
        playerViewModel = ViewModelProvider(this, mediaPlayerViewModelFactory).get(MediaPlayerViewModel::class.java)

        startPostponedEnterTransition()

        return view
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        art.loadFromFileDescriptor(requireArguments().getParcelable("uri")!!)

        playerContainer.radius = AppearancePreferences.getCornerRadius().toFloat()

        ViewUtils.addShadow(playerContainer)

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
                        (art.drawable as AnimatedVectorDrawable).start()
                    }.getOrElse {
                        it.printStackTrace()
                    }
                }
            }

            true
        }

        playerViewModel.getDuration().observe(viewLifecycleOwner, {
            seekBar.max = it
            duration.text = NumberUtils.getFormattedTime(it.toLong().div(durationSmoother))
        })

        playerViewModel.getProgress().observe(viewLifecycleOwner, {
            setSeekbarProgress(it)
            progress.text = NumberUtils.getFormattedTime(it.toLong().div(durationSmoother))
        })

        playerViewModel.getMetadata().observe(viewLifecycleOwner, {
            title.text = it.title
            artist.text = it.artists
            album.text = it.album
            fileInfo.text = getString(R.string.audio_file_info, it.format, it.sampling, it.bitrate)
        })

        playerViewModel.getError().observe(viewLifecycleOwner, {
            val e = Error.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    dismiss()
                }
            })
        })

        playerViewModel.getState().observe(viewLifecycleOwner, {
            buttonStatus(it)
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    this@AudioPlayer.progress.text = NumberUtils.getFormattedTime(progress.toLong().div(durationSmoother))
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playerViewModel.removeProgressCallbacks()
                animation?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                playerViewModel.seek(seekBar.progress.div(durationSmoother))
            }
        })

        playPause.setOnClickListener {
            playerViewModel.changePlayerState()
        }

        playerContainer.setOnClickListener {
            playerViewModel.changePlayerState()
        }

        playerViewModel.getCloseEvent().observe(viewLifecycleOwner, {
            dismiss()
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        requireActivity().finish()
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
