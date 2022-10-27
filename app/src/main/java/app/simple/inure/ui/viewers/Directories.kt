package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.util.FileUtils
import app.simple.inure.util.TextViewUtils.makeClickable

class Directories : ScopedFragment() {

    private lateinit var apkDir: TypeFaceTextView
    private lateinit var dataDir: TypeFaceTextView
    private lateinit var back: DynamicRippleImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_directories, container, false)

        apkDir = view.findViewById(R.id.sub_directory_base_package)
        dataDir = view.findViewById(R.id.sub_directory_data)
        back = view.findViewById(R.id.app_info_back_button)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        apkDir.text = packageInfo.applicationInfo.sourceDir
        dataDir.text = packageInfo.applicationInfo.dataDir

        dataDir.makeClickable(Pair(dataDir.text.toString(), View.OnClickListener {
            kotlin.runCatching {
                FileUtils.openFolder(requireContext(), dataDir.text.toString())
            }.getOrElse {
                it.printStackTrace()
            }
        }))

        back.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): Directories {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = Directories()
            fragment.arguments = args
            return fragment
        }
    }
}