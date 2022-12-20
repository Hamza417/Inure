package app.simple.inure.ui.music

import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.simple.inure.R
import app.simple.inure.activities.association.AudioPlayerActivity
import app.simple.inure.adapters.ui.AdapterMusic
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.KeyboardScopedFragment
import app.simple.inure.popups.music.PopupMusicSort
import app.simple.inure.preferences.MusicPreferences
import app.simple.inure.viewmodels.panels.MusicViewModel

class Music : KeyboardScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterMusic: AdapterMusic? = null
    private val musicViewModel: MusicViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_music, container, false)

        recyclerView = view.findViewById(R.id.music_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        musicViewModel.getSongs().observe(viewLifecycleOwner) {
            adapterMusic = AdapterMusic(it, headerMode = true)

            adapterMusic?.setOnMusicCallbackListener(object : AdapterMusic.Companion.MusicCallbacks {
                override fun onMusicClicked(uri: Uri) {
                    val intent = Intent(requireContext(), AudioPlayerActivity::class.java)
                    intent.data = uri
                    startActivity(intent)
                }

                override fun onMusicSearchClicked() {
                    openFragmentSlide(Search.newInstance(), "search_music")
                }

                override fun onMusicPlayClicked(position: Int) {

                }
            })

            recyclerView.adapter = adapterMusic

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getMusicBottomMenuItems(), recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_sort -> {
                        PopupMusicSort(view)
                    }
                    R.drawable.shuffle -> {
                        musicViewModel.shuffleSongs()
                    }
                    R.drawable.ic_play -> {
                        for (position in it.indices) {
                            if (MusicPreferences.getLastMusicId() == it[position].id) {
                                if (position > 7) {
                                    (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 150)
                                }
                                val intent = Intent(requireContext(), AudioPlayerActivity::class.java)
                                intent.data = Uri.parse(it[position].fileUri)
                                startActivity(intent)
                                break
                            }
                        }
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(), "search_music")
                    }
                }
            }

            startPostponedEnterTransition()
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            MusicPreferences.lastMusicId -> {
                adapterMusic?.updateHighlightedSongState()
            }
            MusicPreferences.musicSort,
            MusicPreferences.musicSortReverse -> {
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
    }
}