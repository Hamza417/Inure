package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import androidx.transition.TransitionManager
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
import app.simple.inure.ui.editor.NotesEditor
import app.simple.inure.ui.viewers.Note
import app.simple.inure.viewmodels.panels.NotesViewModel

class Notes : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterNotes: AdapterNotes? = null
    private var staggeredGridLayoutManager: StaggeredGridLayoutManager? = null
    private lateinit var notesViewModel: NotesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        recyclerView = view.findViewById(R.id.notes_recycler_view)
        notesViewModel = ViewModelProvider(requireActivity())[NotesViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postponeEnterTransition()

        fullVersionCheck()

        notesViewModel.getNotesData().observe(viewLifecycleOwner) { it ->
            adapterNotes = AdapterNotes(it)

            adapterNotes?.setOnItemClickListener(object : AdapterCallbacks {
                override fun onNoteClicked(notesPackageInfo: NotesPackageInfo, view: View) {
                    //                    exitTransition = MaterialElevationScale(false)
                    //                    reenterTransition = MaterialElevationScale(true)
                    //
                    //                    requireActivity().supportFragmentManager.beginTransaction()
                    //                        .addSharedElement(view, notesPackageInfo.packageInfo.packageName)
                    //                        .replace(R.id.app_container, NotesEditor.newInstance(notesPackageInfo.packageInfo))
                    //                        .addToBackStack("notes_editor")
                    //                        .commit()

                    openFragmentArc(NotesEditor.newInstance(notesPackageInfo.packageInfo), view, "notes_editor")
                }

                override fun onNoteLongClicked(notesPackageInfo: NotesPackageInfo, position: Int, view: View) {
                    PopupNotesMenu(requireView()).setOnPopupNotesMenuCallbackListener(object : PopupNotesMenu.Companion.PopupNotesMenuCallback {
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

                        override fun onShareClicked() {
                            ShareCompat.IntentBuilder(requireContext())
                                .setType("text/plain")
                                .setChooserTitle(notesPackageInfo.packageInfo.packageName)
                                .setText(notesPackageInfo.note)
                                .startChooser()
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

            staggeredGridLayoutManager = if (NotesPreferences.getGrid()) {
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            } else {
                StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL)
            }

            staggeredGridLayoutManager?.gapStrategy = StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS
            // recyclerView.addItemDecoration(SpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.popup_padding), true))
            recyclerView.layoutManager = staggeredGridLayoutManager
            recyclerView.itemAnimator = DefaultItemAnimator()
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
            NotesPreferences.isGrid -> {
                if (NotesPreferences.getGrid()) {
                    recyclerView.post {
                        TransitionManager.beginDelayedTransition(recyclerView)
                        staggeredGridLayoutManager?.spanCount = 2
                    }
                } else {
                    recyclerView.post {
                        TransitionManager.beginDelayedTransition(recyclerView)
                        staggeredGridLayoutManager?.spanCount = 1
                    }
                }
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