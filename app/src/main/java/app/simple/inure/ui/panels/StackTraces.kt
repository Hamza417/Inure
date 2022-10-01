package app.simple.inure.ui.panels

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
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.interfaces.adapters.AdapterCallbacks
import app.simple.inure.models.StackTrace
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

        stackTraceViewModel.getStackTraces().observe(viewLifecycleOwner) {
            val adapterStackTraces = AdapterStackTraces(it)

            adapterStackTraces.setOnItemClickListener(object : AdapterCallbacks {
                override fun onSettingsPressed(view: View) {
                    openFragmentSlide(Preferences.newInstance(), "preferences")
                }

                override fun onStackTraceClicked(stackTrace: StackTrace) {
                    val intent = Intent(context, CrashReporterActivity::class.java)
                    intent.putExtra(CrashReporterActivity.MODE_PREVIEW, stackTrace)
                    startActivity(intent)
                }
            })

            recyclerView.adapter = adapterStackTraces

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