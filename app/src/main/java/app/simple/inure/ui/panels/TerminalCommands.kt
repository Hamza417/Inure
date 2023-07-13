package app.simple.inure.ui.panels

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import app.simple.inure.dialogs.terminal.TerminalCreateShortcut.Companion.createTerminalShortcut
import app.simple.inure.dialogs.terminal.TerminalCreateShortcut.Companion.editTerminalShortcut
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.interfaces.terminal.TerminalAddShortcutCallbacks
import app.simple.inure.interfaces.terminal.TerminalCommandCallbacks
import app.simple.inure.models.TerminalCommand
import app.simple.inure.popups.terminal.PopupTerminalCommands
import app.simple.inure.terminal.RunShortcut
import app.simple.inure.terminal.TermDebug
import app.simple.inure.terminal.compat.PRNGFixes
import app.simple.inure.terminal.util.ShortcutEncryption
import app.simple.inure.viewmodels.panels.SavedCommandsViewModel
import java.security.GeneralSecurityException

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
        fullVersionCheck()

        savedCommandsViewModel.getTerminalCommands().observe(viewLifecycleOwner) {
            val adapterTerminalCommands = AdapterTerminalCommands(it)

            adapterTerminalCommands.setOnItemClickListener(object : TerminalCommandCallbacks {
                override fun onCommandClicked(terminalCommand: TerminalCommand?) {
                    runCommand(terminalCommand)
                }

                override fun onCommandLongClicked(terminalCommand: TerminalCommand?, view: View, position: Int) {
                    PopupTerminalCommands(requireView()).setOnPopupNotesMenuCallbackListener(object : PopupTerminalCommands.Companion.PopupTerminalCommandsCallbacks {
                        override fun onDeleteClicked() {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    savedCommandsViewModel.deleteCommand(terminalCommand)
                                    adapterTerminalCommands.removeItem(position)
                                }
                            })
                        }

                        override fun onRunClicked() {
                            runCommand(terminalCommand)
                        }

                        override fun onEditClicked() {
                            childFragmentManager.editTerminalShortcut(terminalCommand!!).setTerminalAddShortcutCallbacks(object : TerminalAddShortcutCallbacks {
                                override fun onCreateShortcut(path: String?, args: String?, label: String?, description: String?) {
                                    val terminalCommandEdited = TerminalCommand(path, args, label, description, terminalCommand.dateCreated)
                                    savedCommandsViewModel.updateCommand(terminalCommandEdited)
                                    adapterTerminalCommands.updateItem(terminalCommandEdited, position)
                                }
                            })
                        }
                    })
                }
            })

            recyclerView.adapter = adapterTerminalCommands

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(BottomMenuConstants.getTerminalCommandsBottomMenuItems(), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_add -> {
                        childFragmentManager.createTerminalShortcut().setTerminalAddShortcutCallbacks(object : TerminalAddShortcutCallbacks {
                            override fun onCreateShortcut(path: String?, args: String?, label: String?, description: String?) {
                                savedCommandsViewModel.addNewCommands(TerminalCommand(path, args, label, description, System.currentTimeMillis()))
                            }
                        })
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

    private fun runCommand(terminalCommand: TerminalCommand?) {
        // Apply workarounds for SecureRandom bugs in Android < 4.4
        PRNGFixes.apply()
        var keys = ShortcutEncryption.getKeys(context)
        if (keys == null) {
            keys = try {
                ShortcutEncryption.generateKeys()
            } catch (e: GeneralSecurityException) {
                Log.e(TermDebug.LOG_TAG, "Generating shortcut encryption keys failed: $e")
                throw RuntimeException(e)
            }
            ShortcutEncryption.saveKeys(context, keys)
        }

        val cmd = StringBuilder()

        if (terminalCommand?.command != null && terminalCommand.command != "") {
            cmd.append(terminalCommand.command)
        }

        if (terminalCommand?.arguments != null && terminalCommand.arguments != "") {
            cmd.append(" ").append(terminalCommand.arguments)
        }

        val cmdStr: String = terminalCommand?.command.toString()
        val encryptedCommand: String

        try {
            encryptedCommand = ShortcutEncryption.encrypt(cmdStr, keys)

            val target = Intent().setClass(requireContext(), RunShortcut::class.java)
            target.action = RunShortcut.ACTION_RUN_SHORTCUT
            target.putExtra(RunShortcut.EXTRA_SHORTCUT_COMMAND, encryptedCommand)
            target.putExtra(RunShortcut.EXTRA_WINDOW_HANDLE, terminalCommand?.label)
            target.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(target)
        } catch (e: GeneralSecurityException) {
            Log.e(TermDebug.LOG_TAG, "Shortcut encryption failed: $e")
            showWarning(e.message.toString(), goBack = false)
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