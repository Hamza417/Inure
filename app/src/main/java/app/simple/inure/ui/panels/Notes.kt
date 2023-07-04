package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterNotes
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.dialogs.notes.NotesMenu
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.popups.notes.PopupNotesMenu
import app.simple.inure.preferences.NotesPreferences
import app.simple.inure.ui.viewers.Note
import app.simple.inure.viewmodels.panels.NotesViewModel

class Notes : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterNotes: AdapterNotes? = null
    private lateinit var notesViewModel: NotesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        recyclerView = view.findViewById(R.id.notes_recycler_view)

        notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fullVersionCheck()

        notesViewModel.getNotesData().observe(viewLifecycleOwner) { it ->
            adapterNotes = AdapterNotes(it)

            adapterNotes?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onNoteClicked(notesPackageInfo: NotesPackageInfo) {
                    openFragmentSlide(NotesEditor.newInstance(notesPackageInfo.packageInfo), "notes_viewer")
                }

                override fun onNoteLongClicked(notesPackageInfo: NotesPackageInfo, position: Int, view: View) {
                    PopupNotesMenu(view).setOnPopupNotesMenuCallbackListener(object : PopupNotesMenu.Companion.PopupNotesMenuCallback {
                        override fun onDeleteClicked() {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    notesViewModel.deleteNoteData(notesPackageInfo, position)
                                }
                            })
                        }

                        override fun onOpenClicked() {
                            openFragmentSlide(Note.newInstance(notesPackageInfo.packageInfo), "notes_editor")
                        }

                        override fun onEditClicked() {
                            openFragmentSlide(NotesEditor.newInstance(notesPackageInfo.packageInfo), "notes_editor")
                        }
                    })
                }
            })

            notesViewModel.getDelete().observe(viewLifecycleOwner) {
                if (it != null) {
                    adapterNotes?.removeItem(it)
                    notesViewModel.clearDelete()
                }
            }

            val staggeredGridLayoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            staggeredGridLayoutManager.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS

            recyclerView.layoutManager = staggeredGridLayoutManager
            // recyclerView.addItemDecoration(SpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.popup_padding), true))
            recyclerView.adapter = adapterNotes

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getGenericBottomMenuItems(), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_settings -> {
                        NotesMenu.newInstance()
                            .show(childFragmentManager, "notes_menu")
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(true), "search")
                    }
                    R.drawable.ic_refresh -> {
                        notesViewModel.refreshNotes()
                    }
                }
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            NotesPreferences.expandedNotes -> {
                adapterNotes?.areNotesExpanded = NotesPreferences.areNotesExpanded()
            }
        }
    }

    companion object {
        fun newInstance(): Notes {
            val args = Bundle()
            val fragment = Notes()
            fragment.arguments = args
            return fragment
        }
    }
}