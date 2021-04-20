package app.simple.inure.ui.app

import android.content.SharedPreferences
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedBottomSheetFragment
import app.simple.inure.packagehelper.PackageUtils.getPackageSize
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import app.simple.inure.util.FileSizeHelper.getFileSize
import app.simple.inure.util.NullSafety.isNotNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Storage : ScopedBottomSheetFragment() {

    private lateinit var apkSize: TypeFaceTextView
    private lateinit var splitsSize: TypeFaceTextView
    private lateinit var cacheSize: TypeFaceTextView
    private lateinit var dataSize: TypeFaceTextView
    private lateinit var apkDir: TypeFaceTextView
    private lateinit var dataDir: TypeFaceTextView

    private lateinit var applicationInfo: ApplicationInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.dialog_storage, container, false)

        apkSize = view.findViewById(R.id.sub_memory_base_package_size)
        splitsSize = view.findViewById(R.id.sub_memory_split_packages)
        cacheSize = view.findViewById(R.id.sub_memory_cache)
        dataSize = view.findViewById(R.id.sub_memory_data)
        apkDir = view.findViewById(R.id.sub_directory_base_package)
        dataDir = view.findViewById(R.id.sub_directory_data)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch {

            var apkSize: String
            var splitsSize: String
            var cacheSize: String
            var dataSize: String
            var apkDir: String
            var dataDir: String

            withContext(Dispatchers.Default) {
                val packageSize = applicationInfo.getPackageSize(requireContext())

                apkSize = packageSize.codeSize.getFileSize()
                splitsSize = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && applicationInfo.splitSourceDirs.isNotNull()) {
                    "${applicationInfo.splitSourceDirs.getDirectorySize()} (${applicationInfo.splitNames.size} Files)"
                } else getString(R.string.not_available)
                cacheSize = packageSize.cacheSize.getFileSize()
                dataSize = packageSize.dataSize.getFileSize()
                apkDir = applicationInfo.sourceDir
                dataDir = applicationInfo.dataDir
            }

            this@Storage.apkSize.text = apkSize
            this@Storage.splitsSize.text = splitsSize
            this@Storage.cacheSize.text = cacheSize
            this@Storage.dataSize.text = dataSize
            this@Storage.apkDir.text = apkDir
            this@Storage.dataDir.text = dataDir
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
