package app.simple.inure.ui.subpanels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterDebloat
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.viewmodels.panels.DebloatViewModel

class DebloatChecklist : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var debloatViewModel: DebloatViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_debloat_checklist, container, false)

        recyclerView = view.findViewById(R.id.checklist_recyclerview)
        debloatViewModel = ViewModelProvider(requireActivity())[DebloatViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        debloatViewModel.getSelectedBloatList().observe(viewLifecycleOwner) { bloats ->
            recyclerView.adapter = AdapterDebloat(bloats, false)
        }
    }

    companion object {
        fun newInstance(): DebloatChecklist {
            val args = Bundle()
            val fragment = DebloatChecklist()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "DebloatChecklist"
    }
}
