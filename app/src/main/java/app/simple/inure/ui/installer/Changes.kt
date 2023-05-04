package app.simple.inure.ui.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.adapters.installer.AdapterInstallerChanges
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.overscroll.CustomVerticalRecyclerView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.util.ParcelUtils.serializable
import app.simple.inure.viewmodels.installer.InstallerChangesViewModel
import java.io.File

class Changes : ScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var installerChangesViewModel: InstallerChangesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_difference, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        val file = requireArguments().serializable<File>(BundleConstants.file)

        val installerViewModelFactory = InstallerViewModelFactory(null, file)
        installerChangesViewModel = ViewModelProvider(requireActivity(), installerViewModelFactory)[InstallerChangesViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        installerChangesViewModel.getChangesData().observe(viewLifecycleOwner) {
            recyclerView.adapter = AdapterInstallerChanges(it)
        }
    }

    companion object {
        fun newInstance(file: File): Changes {
            val args = Bundle()
            args.putSerializable(BundleConstants.file, file)
            val fragment = Changes()
            fragment.arguments = args
            return fragment
        }
    }
}