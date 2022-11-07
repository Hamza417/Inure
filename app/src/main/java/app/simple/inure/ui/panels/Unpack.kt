package app.simple.inure.ui.panels

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.ui.AdapterUnpack
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.panels.UnpackViewModelFactory
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.viewmodels.panels.UnpackViewModel

class Unpack : ScopedFragment() {

    private lateinit var back: DynamicRippleImageButton
    private lateinit var path: TypeFaceTextView
    private lateinit var progress: CustomProgressBar
    private lateinit var recyclerView: RecyclerView

    private lateinit var unpackViewModel: UnpackViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_unpack, container, false)

        back = view.findViewById(R.id.back_button)
        path = view.findViewById(R.id.unpack_path)
        progress = view.findViewById(R.id.data_progress)
        recyclerView = view.findViewById(R.id.unpack_recycler_view)

        packageInfo = requireArguments().parcelable(BundleConstants.packageInfo)!!
        val unpackViewModelFactory = UnpackViewModelFactory(packageInfo)
        unpackViewModel = ViewModelProvider(this, unpackViewModelFactory)[UnpackViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        unpackViewModel.getFileData().observe(viewLifecycleOwner) {
            progress.gone(animate = true)
            val adapterUnpack = AdapterUnpack(it, packageInfo)
            recyclerView.adapter = adapterUnpack
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Unpack {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Unpack()
            fragment.arguments = args
            return fragment
        }
    }
}