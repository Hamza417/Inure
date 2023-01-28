package app.simple.inure.ui.panels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterTerminalCommands
import app.simple.inure.constants.BottomMenuConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.interfaces.terminal.TerminalCommandCallbacks
import app.simple.inure.models.TerminalCommand
import app.simple.inure.viewmodels.panels.SavedCommandsViewModel

class TerminalCommands : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView

    private val savedCommandsViewModel: SavedCommandsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_terminal_commands, container, false)

        recyclerView = view.findViewById(R.id.saved_commands_rv)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedCommandsViewModel.getTerminalCommands().observe(viewLifecycleOwner) {
            val adapterTerminalCommands = AdapterTerminalCommands(it)

            adapterTerminalCommands.setOnItemClickListener(object : TerminalCommandCallbacks {
                override fun onCommandClicked(terminalCommand: TerminalCommand?) {

                }

                override fun onCommandLongClicked(terminalCommand: TerminalCommand?, view: View, minus: Int) {

                }
            })

            recyclerView.adapter = adapterTerminalCommands

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getTerminalCommandsBottomMenuItems(), recyclerView) { id, view ->
                when (id) {
                    R.drawable.ic_add -> {

                    }
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), "prefs_screen")
                    }
                    R.drawable.ic_search -> {
                        openFragmentSlide(Search.newInstance(firstLaunch = true), "search_screen")
                    }
                    R.drawable.ic_clear_all -> {
                        childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                            override fun onSure() {
                                savedCommandsViewModel.deleteAll()
                            }
                        })
                    }
                }
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        fun newInstance(): TerminalCommands {
            val args = Bundle()
            val fragment = TerminalCommands()
            fragment.arguments = args
            return fragment
        }
    }
}