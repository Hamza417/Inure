package app.simple.inure.ui.viewers

import android.animation.ObjectAnimator
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.SeekBar
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.dialogs.miscellaneous.ErrorPopup
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.AudioCoverUtil.loadFromFileDescriptor
import app.simple.inure.util.NumberUtils
import app.simple.inure.viewmodels.dialogs.MediaPlayerViewModel
import app.simple.inure.viewmodels.factory.MediaPlayerViewModelFactory

class AudioPlayer : ScopedFragment() {

    private lateinit var art: ImageView
    private lateinit var playPause: DynamicRippleImageButton
    private lateinit var menu: DynamicRippleImageButton
    private lateinit var duration: TypeFaceTextView
    private lateinit var progress: TypeFaceTextView
    private lateinit var title: TypeFaceTextView
    private lateinit var artist: TypeFaceTextView
    private lateinit var album: TypeFaceTextView
    private lateinit var fileInfo: TypeFaceTextView
    private lateinit var seekBar: SeekBar

    private lateinit var playerViewModel: MediaPlayerViewModel
    private lateinit var mediaPlayerViewModelFactory: MediaPlayerViewModelFactory
    private var animation: ObjectAnimator? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_audio_player, container, false)

        art = view.findViewById(R.id.album_art)
        playPause = view.findViewById(R.id.play_button)
        duration = view.findViewById(R.id.duration)
        progress = view.findViewById(R.id.current_time)
        title = view.findViewById(R.id.title)
        artist = view.findViewById(R.id.artist)
        album = view.findViewById(R.id.album)
        seekBar = view.findViewById(R.id.seekbar)

        mediaPlayerViewModelFactory = MediaPlayerViewModelFactory(requireActivity().application, requireArguments().getParcelable("uri")!!)
        playerViewModel = ViewModelProvider(requireActivity(), mediaPlayerViewModelFactory).get(MediaPlayerViewModel::class.java)

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        art.loadFromFileDescriptor(requireContext(), requireArguments().getParcelable("uri")!!)

        playerViewModel.getDuration().observe(viewLifecycleOwner, {
            seekBar.max = it
            duration.text = NumberUtils.getFormattedTime(it.toLong())
        })

        playerViewModel.getProgress().observe(viewLifecycleOwner, {
            setSeekbarProgress(it)
            progress.text = NumberUtils.getFormattedTime(it.toLong())
        })

        playerViewModel.getMetadata().observe(viewLifecycleOwner, {
            title.text = it.title
            artist.text = it.artists
            album.text = it.album
            //fileInfo.text = getString(R.string.audio_file_info, it.format, it.sampling, it.bitrate)
        })

        playerViewModel.getError().observe(viewLifecycleOwner, {
            val e = ErrorPopup.newInstance(it)
            e.show(childFragmentManager, "error_dialog")
            e.setOnErrorDialogCallbackListener(object : ErrorPopup.Companion.ErrorDialogCallbacks {
                override fun onDismiss() {
                    requireActivity().onBackPressed()
                }
            })
        })

        playerViewModel.getState().observe(viewLifecycleOwner, {
            buttonStatus(it)
        })

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    this@AudioPlayer.progress.text = NumberUtils.getFormattedTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                playerViewModel.removeProgressCallbacks()
                animation?.cancel()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
                playerViewModel.seek(seekBar.progress)
            }
        })

        playPause.setOnClickListener {
            playerViewModel.changePlayerState()
        }

        playerViewModel.getCloseEvent().observe(viewLifecycleOwner, {
            requireActivity().finish()
        })
    }

    private fun setSeekbarProgress(seekbarProgress: Int) {
        animation = ObjectAnimator.ofInt(seekBar, "progress", seekbarProgress)
        animation!!.duration = 1000L
        animation!!.interpolator = DecelerateInterpolator(1.5F)
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
