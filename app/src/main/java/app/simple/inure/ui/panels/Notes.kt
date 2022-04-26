package app.simple.inure.ui.panels

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterNotes
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.Sure
import app.simple.inure.dialogs.menus.NotesMenu
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AppsAdapterCallbacks
import app.simple.inure.models.NotesPackageInfo
import app.simple.inure.preferences.NotesPreferences
import app.simple.inure.ui.viewers.Note
import app.simple.inure.util.FragmentHelper
import app.simple.inure.viewmodels.panels.NotesViewModel

class Notes : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private var adapterNotes: AdapterNotes? = null
    private lateinit var notesViewModel: NotesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_notes, container, false)

        recyclerView = view.findViewById(R.id.notes_recycler_view)

        notesViewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notesViewModel.getNotesData().observe(viewLifecycleOwner) {
            adapterNotes = AdapterNotes(it)

            adapterNotes?.setOnItemClickListener(object : AppsAdapterCallbacks {
                override fun onNoteClicked(notesPackageInfo: NotesPackageInfo) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                NotesEditor.newInstance(notesPackageInfo.packageInfo),
                                                "notes_editor")
                }

                override fun onNoteLongClicked(notesPackageInfo: NotesPackageInfo) {
                    clearExitTransition()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                Note.newInstance(notesPackageInfo.packageInfo),
                                                "notes_viewer")
                }

                override fun onSearchPressed(view: View) {
                    clearTransitions()
                    FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                                Search.newInstance(true),
                                                "search")
                }

                override fun onSettingsPressed(view: View) {
                    NotesMenu.newInstance()
                        .show(childFragmentManager, "notes_menu")
                }

                override fun onNoteDelete(view: View, notesPackageInfo: NotesPackageInfo?) {
                    val p = Sure.newInstance()

                    p.setOnSureCallbackListener(object : Sure.Companion.SureCallbacks {
                        override fun onSure() {
                            notesViewModel.deleteNoteData(notesPackageInfo)
                        }
                    })

                    p.show(childFragmentManager, "sure")
                }
            })

            recyclerView.adapter = adapterNotes

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