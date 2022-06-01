package app.simple.inure.ui.preferences.subscreens

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.preferences.AdapterShortcuts
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.viewmodels.preferences.ShortcutsViewModel

class Shortcuts : ScopedFragment() {

    private lateinit var recyclerView: RecyclerView
    private val shortcutsViewModel: ShortcutsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.sub_preferences_shortcuts, container, false)

        recyclerView = view.findViewById(R.id.shortcuts_recycler_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        shortcutsViewModel.getShortcuts().observe(viewLifecycleOwner) {
            val shortcuts = ShortcutManagerCompat.getDynamicShortcuts(requireContext())
            val adapterShortcuts = AdapterShortcuts(it, shortcuts)

            recyclerView.adapter = adapterShortcuts
        }
    }

    companion object {
        fun newInstance(): Shortcuts {
            val args = Bundle()
            val fragment = Shortcuts()
            fragment.arguments = args
            return fragment
        }
    }
}