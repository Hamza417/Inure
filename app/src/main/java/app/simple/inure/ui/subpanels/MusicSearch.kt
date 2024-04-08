package app.simple.inure.ui.subpanels

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.app.SharedElementCallback
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.music.AdapterMusic
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.interfaces.menus.PopupMusicMenuCallbacks
import app.simple.inure.models.AudioModel
import app.simple.inure.popups.music.PopupMusicMenu
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.services.AudioServicePager
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.MusicViewModel

class MusicSearch : KeyboardScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var searchBox: TypeFaceEditText
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var searchContainer: LinearLayout

    private lateinit var musicViewModel: MusicViewModel
    private var adapterMusic: AdapterMusic? = null

    private var deletedId = -1L
    private var displayHeight: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_music_search, container, false)

        recyclerView = view.findViewById(R.id.search_recycler_view)
        searchBox = view.findViewById(R.id.search_box)
        clear = view.findViewById(R.id.clear)
        searchContainer = view.findViewById(R.id.search_container)
        musicViewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]

        displayHeight = StatusBarHeight.getDisplayHeight(requireContext()) +
                StatusBarHeight.getStatusBarHeight(requireContext().resources)

        //        val params = searchContainer.layoutParams as ViewGroup.MarginLayoutParams
        //        params.setMargins(params.leftMargin,
        //                          StatusBarHeight.getStatusBarHeight(resources) + params.topMargin,
        //                          params.rightMargin,
        //                          params.bottomMargin)
        //
        //        recyclerView.setPadding(recyclerView.paddingLeft,
        //                                recyclerView.paddingTop + params.topMargin + params.height + params.bottomMargin,
        //                                recyclerView.paddingRight,
        //                                recyclerView.paddingBottom)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        searchBox.setText(MusicPreferences.getSearchKeyword())
        searchBox.setWindowInsetsAnimationCallback()
        clearButtonState()

        if (requireArguments().getBoolean(BundleConstants.isKeyboardOpened, false).invert()) {
            searchBox.showInput()
            requireArguments().putBoolean(BundleConstants.isKeyboardOpened, true)
        }

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                MusicPreferences.setSearchKeyword(text.toString())
            }

            clearButtonState()
        }

        musicViewModel.getSearched().observe(viewLifecycleOwner) {
            adapterMusic = AdapterMusic(it, false)

            adapterMusic?.setOnMusicCallbackListener(object : AdapterMusic.Companion.MusicCallbacks {
                override fun onMusicClicked(audioModel: AudioModel, art: ImageView, position: Int) {
                    openFragmentArc(AudioPlayer.newInstance(position, fromSearch = true), art, "audio_player_pager")
                    requireArguments().putInt(BundleConstants.position, position)
                }

                override fun onMusicLongClicked(audioModel: AudioModel, view: ImageView, position: Int, container: View) {
                    PopupMusicMenu(requireView(), audioModel.fileUri.toUri()).setOnPopupMusicMenuCallbacks(object : PopupMusicMenuCallbacks {
                        override fun onPlay(uri: Uri) {
                            openFragmentArc(AudioPlayer.newInstance(position, fromSearch = true), view, "audio_player_pager")
                            MusicPreferences.setMusicPosition(position)
                            MusicPreferences.setLastMusicId(audioModel.id)
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

            recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(view: View, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int) {
                    if (savedInstanceState != null) {
                        recyclerView.removeOnLayoutChangeListener(this)
                        val layoutManager = recyclerView.layoutManager
                        val viewAtPosition = layoutManager!!.findViewByPosition(MusicPreferences.getMusicPosition())

                        // Scroll to position if the view for the current position is null
                        // (not currently part of layout manager children), or it's not completely
                        // visible.
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
                    }
                }
            })

            if (DevelopmentPreferences.get(DevelopmentPreferences.usePeristyleInterface)) {
                recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
            } else {
                recyclerView.layoutManager = LinearLayoutManager(requireContext())
            }

            recyclerView.adapter = adapterMusic

            if (requireArguments().getInt(BundleConstants.position, MusicPreferences.getMusicPosition()) == MusicPreferences.getMusicPosition()) {
                (view.parent as? ViewGroup)?.doOnPreDraw {
                    startPostponedEnterTransition()
                }
            }
        }

        musicViewModel.getDeleted().observe(viewLifecycleOwner) {
            if (deletedId != -1L) {
                adapterMusic?.updateDeleted(it)

                if (deletedId == MusicPreferences.getLastMusicId()) {
                    try {
                        requireContext().stopService(Intent(requireContext(), AudioServicePager::class.java))
                    } catch (e: IllegalStateException) {
                        e.printStackTrace()
                    }
                }

                deletedId = -1L
            }
        }

        clear.setOnClickListener {
            searchBox.text?.clear()
            MusicPreferences.setSearchKeyword("")
        }

        setExitSharedElementCallback(object : SharedElementCallback() {
            override fun onMapSharedElements(names: MutableList<String>, sharedElements: MutableMap<String, View>) {
                // Locate the ViewHolder for the clicked position.
                val selectedViewHolder = recyclerView.findViewHolderForAdapterPosition(MusicPreferences.getMusicPosition())
                if (selectedViewHolder is AdapterMusic.Holder) {
                    // Map the first shared element name to the child ImageView.
                    sharedElements[names[0]] = selectedViewHolder.art
                }
            }
        })
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MusicPreferences.SEARCH_KEYWORD -> {
                musicViewModel.loadSearched(MusicPreferences.getSearchKeyword())
            }
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

    private fun clearButtonState() {
        if (searchBox.text.isNullOrEmpty()) {
            clear.gone(animate = true)
        } else {
            clear.visible(animate = true)
        }
    }

    companion object {
        fun newInstance(): MusicSearch {
            val args = Bundle()
            val fragment = MusicSearch()
            fragment.arguments = args
            return fragment
        }
    }
}
