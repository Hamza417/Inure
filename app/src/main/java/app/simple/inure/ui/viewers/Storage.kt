package app.simple.inure.ui.viewers

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.getPackageSize
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.Pie
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import app.simple.inure.util.FileSizeHelper.toSize
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.viewers.AppSizeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Storage : ScopedFragment() {

    private lateinit var apkSize: TypeFaceTextView
    private lateinit var splitsSize: TypeFaceTextView
    private lateinit var cacheSize: TypeFaceTextView
    private lateinit var dataSize: TypeFaceTextView
    private lateinit var back: DynamicRippleImageButton
    private lateinit var pie: Pie

    private val model: AppSizeViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_storage, container, false)

        apkSize = view.findViewById(R.id.sub_memory_base_package_size)
        splitsSize = view.findViewById(R.id.sub_memory_split_packages)
        cacheSize = view.findViewById(R.id.sub_memory_cache)
        dataSize = view.findViewById(R.id.sub_memory_data)
        back = view.findViewById(R.id.app_info_back_button)
        pie = view.findViewById(R.id.storage_apk_size_pie)

        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        model.getTotalAppSize().observe(requireActivity(), {
            val x = packageInfo.applicationInfo.sourceDir.getDirectoryLength().toDouble() / it.toDouble() * 100.0
            pie.value = (x * (360.0 / 100.0)).toFloat()
        })

        viewLifecycleOwner.lifecycleScope.launch {

            var apkSize: String
            var splitsSize: String
            var cacheSize: String
            var dataSize: String

            withContext(Dispatchers.Default) {
                val packageSize = packageInfo.getPackageSize(requireContext())

                apkSize = packageSize.codeSize.toSize()
                splitsSize = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && packageInfo.applicationInfo.splitSourceDirs.isNotNull()) {
                    "${packageInfo.applicationInfo.splitSourceDirs.getDirectorySize()} (${packageInfo.splitNames.size} ${getString(R.string.files)})"
                } else getString(R.string.not_available)
                cacheSize = packageSize.cacheSize.toSize()
                dataSize = packageSize.dataSize.toSize()
            }

            this@Storage.apkSize.text = apkSize
            this@Storage.splitsSize.text = splitsSize
            this@Storage.cacheSize.text = cacheSize
            this@Storage.dataSize.text = dataSize
        }


        back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    companion object {
        fun newInstance(applicationInfo: PackageInfo): Storage {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, applicationInfo)
            val fragment = Storage()
            fragment.arguments = args
            return fragment
        }
    }
}
