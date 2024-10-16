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
import app.simple.inure.extensions.fragments.InstallerLoaderScopedFragment
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.util.ParcelUtils.serializable
import app.simple.inure.viewmodels.installer.InstallerChangesViewModel
import java.io.File

class Changes : InstallerLoaderScopedFragment() {

    private lateinit var recyclerView: CustomVerticalRecyclerView
    private lateinit var installerChangesViewModel: InstallerChangesViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_difference, container, false)

        recyclerView = view.findViewById(R.id.recycler_view)

        val file = requireArguments().serializable<File>(BundleConstants.file)

        val installerViewModelFactory = InstallerViewModelFactory(null, file)
        installerChangesViewModel = ViewModelProvider(this, installerViewModelFactory)[InstallerChangesViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        installerChangesViewModel.getChangesData().observe(viewLifecycleOwner) {
            onLoadingFinished()
            recyclerView.adapter = AdapterInstallerChanges(it)
        }
    }

    override fun setupBackPressedDispatcher() {
        /* no-op */
    }

    override fun setupBackPressedCallback(view: ViewGroup) {
        /* no-op */
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
