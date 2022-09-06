package app.simple.inure.ui.installer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.views.LineNumberEditText
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.installer.InstallerManifestViewModelFactory
import app.simple.inure.viewmodels.installer.InstallerManifestViewModel
import java.io.File

class Manifest : ScopedFragment() {

    private lateinit var text: LineNumberEditText
    private lateinit var manifestViewModel: InstallerManifestViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.installer_fragment_manifest, container, false)

        text = view.findViewById(R.id.line_edit_text)
        val p0 = InstallerManifestViewModelFactory(requireArguments().getSerializable(BundleConstants.file) as File)
        manifestViewModel = ViewModelProvider(this, p0)[InstallerManifestViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        manifestViewModel.getSpanned().observe(viewLifecycleOwner) {
            text.setText(it)
        }
    }

    companion object {
        fun newInstance(file: File): Manifest {
            val args = Bundle()
            args.putSerializable(BundleConstants.file, file)
            val fragment = Manifest()
            fragment.arguments = args
            return fragment
        }
    }
}