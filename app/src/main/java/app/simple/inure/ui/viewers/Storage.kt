package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.Pie
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.FileSizeHelper.getDirectoryLength
import app.simple.inure.util.PackageUtils.getPackageSize
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import app.simple.inure.util.FileSizeHelper.getFileSize
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.viewmodels.AppSize
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

    private val model: AppSize by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_storage, container, false)

        apkSize = view.findViewById(R.id.sub_memory_base_package_size)
        splitsSize = view.findViewById(R.id.sub_memory_split_packages)
        cacheSize = view.findViewById(R.id.sub_memory_cache)
        dataSize = view.findViewById(R.id.sub_memory_data)
        back = view.findViewById(R.id.app_info_back_button)
        pie = view.findViewById(R.id.storage_apk_size_pie)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        model.getTotalAppSize().observe(requireActivity(), {
            val x = applicationInfo.sourceDir.getDirectoryLength().toDouble() / it.toDouble() * 100.0
            pie.value = (x * (360.0 / 100.0)).toFloat()
        })

        viewLifecycleOwner.lifecycleScope.launch {

            var apkSize: String
            var splitsSize: String
            var cacheSize: String
            var dataSize: String

            withContext(Dispatchers.Default) {
                val packageSize = applicationInfo.getPackageSize(requireContext())

                apkSize = packageSize.codeSize.getFileSize()
                splitsSize = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && applicationInfo.splitSourceDirs.isNotNull()) {
                    "${applicationInfo.splitSourceDirs.getDirectorySize()} (${applicationInfo.splitNames.size} ${getString(R.string.files)})"
                } else getString(R.string.not_available)
                cacheSize = packageSize.cacheSize.getFileSize()
                dataSize = packageSize.dataSize.getFileSize()
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
        fun newInstance(applicationInfo: ApplicationInfo): Storage {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Storage()
            fragment.arguments = args
            return fragment
        }
    }
}
