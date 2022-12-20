package app.simple.inure.ui.panels

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.doOnPreDraw
import androidx.fragment.app.viewModels
import app.simple.inure.R
import app.simple.inure.activities.app.CrashReporterActivity
import app.simple.inure.adapters.ui.AdapterStackTraces
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.dialogs.app.Sure.Companion.newSureInstance
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.interfaces.popups.PopupStackTracesCallbacks
import app.simple.inure.models.StackTrace
import app.simple.inure.popups.stacktraces.PopupStackTracesMenu
import app.simple.inure.viewmodels.panels.StackTraceViewModel

class StackTraces : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private val stackTraceViewModel: StackTraceViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_stacktraces, container, false)

        recyclerView = view.findViewById(R.id.stacktraces_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        stackTraceViewModel.getStackTraces().observe(viewLifecycleOwner) { it ->
            val adapterStackTraces = AdapterStackTraces(it)

            adapterStackTraces.setOnItemClickListener(object : AdapterCallbacks {
                override fun onStackTraceClicked(stackTrace: StackTrace) {
                    val intent = Intent(context, CrashReporterActivity::class.java)
                    intent.putExtra(CrashReporterActivity.MODE_PREVIEW, stackTrace)
                    startActivity(intent)
                }

                override fun onStackTraceLongClicked(stackTrace: StackTrace, view: View, position: Int) {
                    PopupStackTracesMenu(view).setOnPopupStackTracesCallbacks(object : PopupStackTracesCallbacks {
                        override fun onDelete() {
                            childFragmentManager.newSureInstance().setOnSureCallbackListener(object : SureCallbacks {
                                override fun onSure() {
                                    stackTraceViewModel.deleteStackTrace(stackTrace, position)
                                }

                                override fun onCancel() {
                                    // do nothing
                                }
                            })
                        }

                        override fun onSend() {
                            val shareIntent = Intent(Intent.ACTION_SEND)
                            shareIntent.type = "text/plain"
                            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.the_app_has_crashed))
                            shareIntent.putExtra(Intent.EXTRA_TEXT, stackTrace.trace.trim().trimIndent())
                            startActivity(Intent.createChooser(shareIntent, "Crash Log"))
                        }

                        override fun onCopy() {
                            // Copy to clipboard
                            val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipData = ClipData.newPlainText("Stack Traces", stackTrace.trace)
                            clipBoard.setPrimaryClip(clipData)
                        }

                        override fun onOpen() {
                            onStackTraceClicked(stackTrace)
                        }
                    })
                }
            })

            stackTraceViewModel.getDelete().observe(viewLifecycleOwner) {
                adapterStackTraces.itemRemoved(it)
            }

            recyclerView.adapter = adapterStackTraces

            bottomRightCornerMenu?.initBottomMenuWithRecyclerView(arrayListOf(R.drawable.ic_settings), recyclerView) { id, _ ->
                when (id) {
                    R.drawable.ic_settings -> {
                        openFragmentSlide(Preferences.newInstance(), "prefs_screen")
                    }
                }
            }

            (view.parent as? ViewGroup)?.doOnPreDraw {
                startPostponedEnterTransition()
            }
        }
    }

    companion object {
        fun newInstance(): StackTraces {
            val args = Bundle()
            val fragment = StackTraces()
            fragment.arguments = args
            return fragment
        }
    }
}