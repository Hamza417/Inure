package app.simple.inure.ui.music

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.net.toUri
import androidx.core.view.doOnPreDraw
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterMusic
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceEditText
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.interfaces.menus.PopupMusicMenuCallbacks
import app.simple.inure.models.AudioModel
import app.simple.inure.popups.music.PopupMusicMenu
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.ui.viewers.AudioPlayer
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.panels.MusicViewModel

class Search : KeyboardScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var searchBox: TypeFaceEditText
    private lateinit var clear: DynamicRippleImageButton
    private lateinit var searchContainer: LinearLayout
    private lateinit var musicViewModel: MusicViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_music_search, container, false)

        recyclerView = view.findViewById(R.id.search_recycler_view)
        searchBox = view.findViewById(R.id.search_box)
        clear = view.findViewById(R.id.clear)
        searchContainer = view.findViewById(R.id.search_container)
        musicViewModel = ViewModelProvider(requireActivity())[MusicViewModel::class.java]

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
        searchBox.showInput()
        clearButtonState()

        searchBox.doOnTextChanged { text, _, _, _ ->
            if (searchBox.isFocused) {
                MusicPreferences.setSearchKeyword(text.toString())
            }

            clearButtonState()
        }

        musicViewModel.getSearched().observe(viewLifecycleOwner) {
            val adapterMusic = AdapterMusic(it, false)

            adapterMusic.setOnMusicCallbackListener(object : AdapterMusic.Companion.MusicCallbacks {
                override fun onMusicClicked(uri: Uri, art: ImageView) {
                    openFragmentArc(AudioPlayer.newInstance(uri), art, "audio_player")
                }

                override fun onMusicLongClicked(audioModel: AudioModel, view: ImageView, position: Int) {
                    PopupMusicMenu(view, audioModel.fileUri.toUri()).setOnPopupMusicMenuCallbacks(object : PopupMusicMenuCallbacks {
                        override fun onPlay(uri: Uri) {
                            openFragmentArc(AudioPlayer.newInstance(uri), view, "audio_player")
                            MusicPreferences.setLastMusicId(audioModel.id)
                        }

                        override fun onDelete(uri: Uri) {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
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

            recyclerView.adapter = adapterMusic

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }

        clear.setOnClickListener {
            searchBox.text?.clear()
            MusicPreferences.setSearchKeyword("")
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MusicPreferences.searchKeyword -> {
                musicViewModel.loadSearched(MusicPreferences.getSearchKeyword())
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
        fun newInstance(): Search {
            val args = Bundle()
            val fragment = Search()
            fragment.arguments = args
            return fragment
        }
    }
}