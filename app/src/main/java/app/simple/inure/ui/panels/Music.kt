package app.simple.inure.ui.panels

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLayoutChangeListener
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.app.SharedElementCallback
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.music.AdapterMusic
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.miscellaneous.StoragePermission
import app.simple.inure.dialogs.miscellaneous.StoragePermission.Companion.showStoragePermissionDialog
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.interfaces.menus.PopupMusicMenuCallbacks
import app.simple.inure.models.AudioModel
import app.simple.inure.popups.music.PopupMusicMenu
import app.simple.inure.popups.music.PopupMusicSort
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.services.AudioServicePager
import app.simple.inure.ui.subpanels.MusicSearch
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.PermissionUtils.checkStoragePermission
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.viewmodels.panels.MusicViewModel

class Music : KeyboardScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterMusic: AdapterMusic? = null
    private val musicViewModel: MusicViewModel by activityViewModels()

    private var deletedId = -1L
    private var displayHeight: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_music, container, false)

        recyclerView = view.findViewById(R.id.music_recycler_view)
        displayHeight = StatusBarHeight.getDisplayHeight(requireContext()) +
                StatusBarHeight.getStatusBarHeight(requireContext().resources)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        if (fullVersionCheck()) {
            if (requireContext().checkStoragePermission()) {
                if (musicViewModel.shouldShowLoader()) {
                    showLoader(true)
                }
            } else {
                childFragmentManager.showStoragePermissionDialog()
                    .setStoragePermissionCallbacks(object : StoragePermission.Companion.StoragePermissionCallbacks {
                        override fun onStoragePermissionGranted() {
                            showLoader(true)
                            musicViewModel.refresh()
                        }
                    })
            }
        }

        musicViewModel.getSongs().observe(viewLifecycleOwner) { audioModels ->
            hideLoader()
            adapterMusic = AdapterMusic(audioModels, headerMode = true)

            adapterMusic?.setOnMusicCallbackListener(object : AdapterMusic.Companion.MusicCallbacks {
                override fun onMusicClicked(audioModel: AudioModel, art: ImageView, position: Int) {
                    openAudioPlayer(position, art)
                }

                override fun onMusicLongClicked(audioModel: AudioModel, view: ImageView, position: Int, container: View) {
                    PopupMusicMenu(requireView(), audioModel.fileUri.toUri()).setOnPopupMusicMenuCallbacks(object : PopupMusicMenuCallbacks {
                        override fun onPlay(uri: Uri) {
                            openAudioPlayer(position, view)
                        }

                        override fun onDelete(uri: Uri) {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    deletedId = audioModel.id
                                    musicViewModel.deleteSong(uri, position)
                                }
                            })
                        }

                        override fun onShare(uri: Uri) {
                            val intent = Intent(Intent.ACTION_SEND)
                            intent.type = "audio/*"
                            intent.putExtra(Intent.EXTRA_STREAM, uri)
                            startActivity(Intent.createChooser(intent, audioModel.title + " " + audioModel.artists))
                        }
                    })
                }
            })

            recyclerView.addOnLayoutChangeListener(object : OnLayoutChangeListener {
                override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                    if (requireArguments().getBoolean(BundleConstants.firstLaunch, true).invert()) { // Make sure first launch doesn't jump to position
                        recyclerView.removeOnLayoutChangeListener(this)
                        val layoutManager = recyclerView.layoutManager
                        val viewAtPosition = layoutManager!!.findViewByPosition(MusicPreferences.getMusicPosition())

                        /**
                         * Scroll to position if the view for the current position is null
                         * (not currently part of layout manager children), or it's not completely
                         * visible.
                         */
                        if (viewAtPosition == null || layoutManager.isViewPartiallyVisible(viewAtPosition, false, true)) {
                            recyclerView.post {
                                // Log.d("Music", displayHeight.toString())
                                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(MusicPreferences.getMusicPosition(), displayHeight / 2)

                                (view.parent as? ViewGroup)?.doOnPreDraw {
                                    startPostponedEnterTransition()
                                }
                            }
                        } else {
                            (view.parent as? ViewGroup)?.doOnPreDraw {
                                startPostponedEnterTransition()
                            }
                        }
                    } else {
                        (view.parent as? ViewGroup)?.doOnPreDraw {
                            startPostponedEnterTransition()
                        }

                        recyclerView.removeOnLayoutChangeListener(this)
                        requireArguments().putBoolean(BundleConstants.firstLaunch, false)
                    }
                }
            })

            if (DevelopmentPreferences.get(DevelopmentPreferences.USE_PERISTYLE_INTERFACE)) {
                if (recyclerView.layoutManager == null || recyclerView.layoutManager !is GridLayoutManager) {
                    val gridLayoutManager = GridLayoutManager(requireContext(), 2)

                    gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return if (position % 5 == 0) {
                                2
                            } else {
                                1
                            }
                        }
                    }

                    recyclerView.layoutManager = gridLayoutManager

                    // Remove fading edge effect
                    recyclerView.isVerticalFadingEdgeEnabled = false
                }
            }

            recyclerView.adapter = adapterMusic

            if (requireArguments().getInt(BundleConstants.position, MusicPreferences.getMusicPosition()) == MusicPreferences.getMusicPosition()) {
                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getMusicBottomMenuItems(), recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_refresh -> {
                        requireArguments().putBoolean(BundleConstants.firstLaunch, true) // This will prevent jumping to position
                        musicViewModel.refresh()
                    }
                    R.drawable.ic_sort -> {
                        PopupMusicSort(view)
                    }
                    R.drawable.shuffle -> {
                        val randomPosition = (0 until audioModels.size).random()
                        MusicPreferences.setMusicPosition(randomPosition)
                        (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(randomPosition, displayHeight / 2)

                        postDelayed(500) {
                            runCatching {
                                val viewHolder = recyclerView.findViewHolderForAdapterPosition(randomPosition) as AdapterMusic.Holder
                                openAudioPlayer(randomPosition, viewHolder.itemView.findViewById(R.id.adapter_music_art))
                            }.onFailure {
                                showError(it, goBack = false)
                            }
                        }
                    }
                    R.drawable.ic_play -> {
                        for (position in audioModels.indices) {
                            if (MusicPreferences.getLastMusicId() == audioModels[position].id) {
                                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, displayHeight / 2)

                                postDelayed(500) {
                                    runCatching {
                                        val viewHolder = recyclerView.findViewHolderForAdapterPosition(position) as AdapterMusic.Holder
                                        openAudioPlayer(MusicPreferences.getMusicPosition(), viewHolder.itemView.findViewById(R.id.adapter_music_art))
                                    }.onFailure {
                                        showError(it, goBack = false)
                                    }
                                }

                                break
                            }
                        }
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(MusicSearch.newInstance(), MusicSearch.TAG)
                    }
                }
            }
        }

        musicViewModel.getDeleted().observe(viewLifecycleOwner) {
            if (it != -1) {
                adapterMusic?.updateDeleted(it)

                if (deletedId == MusicPreferences.getLastMusicId()) {
                    try {
                        requireContext().stopService(Intent(requireContext(), AudioServicePager::class.java))
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }

                musicViewModel.setDeleted(-1)
            }
        }

        musicViewModel.getError().observe(viewLifecycleOwner) {
            showError(it, goBack = false)
        }

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                // Locate the ViewHolder for the clicked position.
                val selectedViewHolder = recyclerView.findViewHolderForAdapterPosition(MusicPreferences.getMusicPosition().plus(1))
                if (selectedViewHolder is AdapterMusic.Holder) {
                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] = selectedViewHolder.art
                }
            }
        })
    }

    private fun openAudioPlayer(position: Int, view: View) {
        requireArguments().putInt(BundleConstants.position, position)
        openFragmentArc(AudioPlayer.newInstance(position), view, AudioPlayer.TAG)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MusicPreferences.LAST_MUSIC_ID -> {
                adapterMusic?.updateHighlightedSongState()
            }
            MusicPreferences.MUSIC_SORT,
            MusicPreferences.MUSIC_SORT_REVERSE -> {
                MusicPreferences.setMusicPosition(-1)
                musicViewModel.sortSongs()
            }
        }
    }

    companion object {
        fun newInstance(): Music {
            val args = Bundle()
            val fragment = Music()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "Music"
    }
}
