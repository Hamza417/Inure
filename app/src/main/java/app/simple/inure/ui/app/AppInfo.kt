package app.simple.inure.ui.app

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.CoroutineScopedFragment
import app.simple.inure.glide.util.AppIconExtensions.loadAppIcon
import app.simple.inure.packagehelper.PackageUtils
import app.simple.inure.packagehelper.PackageUtils.getApplicationInstallTime
import app.simple.inure.packagehelper.PackageUtils.getPackageSize
import app.simple.inure.util.FileSizeHelper.getDirectorySize
import app.simple.inure.util.FileSizeHelper.getFileSize
import app.simple.inure.util.FolderHelper
import app.simple.inure.util.NullSafety.isNotNull
import app.simple.inure.util.SDKHelper
import app.simple.inure.util.TextViewUtils.makeLinks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppInfo : CoroutineScopedFragment() {

    private lateinit var icon: ImageView

    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView

    private lateinit var version: TypeFaceTextView
    private lateinit var versionCode: TypeFaceTextView
    private lateinit var installLocation: TypeFaceTextView
    private lateinit var minSdk: TypeFaceTextView
    private lateinit var targetSdk: TypeFaceTextView
    private lateinit var uid: TypeFaceTextView
    private lateinit var installDate: TypeFaceTextView
    private lateinit var updateDate: TypeFaceTextView

    private lateinit var apkSize: TypeFaceTextView
    private lateinit var splitsSize: TypeFaceTextView
    private lateinit var cacheSize: TypeFaceTextView
    private lateinit var dataSize: TypeFaceTextView
    private lateinit var apkDir: TypeFaceTextView
    private lateinit var dataDir: TypeFaceTextView

    private lateinit var applicationInfo: ApplicationInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_app_info, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageId = view.findViewById(R.id.fragment_app_package_id)
        version = view.findViewById(R.id.sub_information_version)
        versionCode = view.findViewById(R.id.sub_information_version_code)
        installLocation = view.findViewById(R.id.sub_information_install_location)
        minSdk = view.findViewById(R.id.sub_information_min_sdk)
        targetSdk = view.findViewById(R.id.sub_information_target_sdk)
        uid = view.findViewById(R.id.sub_information_uid)
        installDate = view.findViewById(R.id.sub_information_install_date)
        updateDate = view.findViewById(R.id.sub_information_update_date)
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

        icon.transitionName = requireArguments().getString("transition_name")
        icon.loadAppIcon(requireContext(), applicationInfo.packageName)
        startPostponedEnterTransition()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            "${applicationInfo.minSdkVersion}, ${SDKHelper.getSdkTitle(applicationInfo.minSdkVersion)}".also { minSdk.text = it }
        }
        "${applicationInfo.targetSdkVersion}, ${SDKHelper.getSdkTitle(applicationInfo.targetSdkVersion)}".also { targetSdk.text = it }

        launch {
            var name: String
            var packageId: String
            var version: String
            var versionCode: String
            var installLocation: String
            var uid: String
            var installDate: String
            var updateDate: String
            var apkSize: String
            var splitsSize: String
            var cacheSize: String
            var dataSize: String
            var apkDir: String
            var dataDir: String

            withContext(Dispatchers.Default) {
                val packageSize = applicationInfo.getPackageSize(requireContext())

                name = applicationInfo.name
                packageId = applicationInfo.packageName
                version = PackageUtils.getApplicationVersion(requireContext(), applicationInfo)
                versionCode = PackageUtils.getApplicationVersionCode(requireContext(), applicationInfo)
                installLocation = applicationInfo.publicSourceDir
                uid = applicationInfo.uid.toString()
                installDate = applicationInfo.getApplicationInstallTime(requireContext())
                updateDate = PackageUtils.getApplicationLastUpdateTime(requireContext(), applicationInfo)
                apkSize = applicationInfo.sourceDir.getFileSize()
                splitsSize = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O && applicationInfo.splitSourceDirs.isNotNull()) {
                    "${applicationInfo.splitSourceDirs.getDirectorySize()} (${applicationInfo.splitNames.size} Files)"
                } else getString(R.string.not_available)
                cacheSize = packageSize.cacheSize.getFileSize()
                dataSize = packageSize.dataSize.getFileSize()
                apkDir = applicationInfo.sourceDir
                dataDir = applicationInfo.dataDir
            }

            this@AppInfo.name.text = name
            this@AppInfo.packageId.text = packageId
            this@AppInfo.version.text = version
            this@AppInfo.versionCode.text = versionCode
            this@AppInfo.installLocation.text = installLocation
            this@AppInfo.uid.text = uid
            this@AppInfo.installDate.text = installDate
            this@AppInfo.updateDate.text = updateDate
            this@AppInfo.apkSize.text = apkSize
            this@AppInfo.splitsSize.text = splitsSize
            this@AppInfo.cacheSize.text = cacheSize
            this@AppInfo.dataSize.text = dataSize
            this@AppInfo.apkDir.text = apkDir
            this@AppInfo.dataDir.text = dataDir
        }
    }

    companion object {
        fun newInstance(packageName: ApplicationInfo, transitionName: String): AppInfo {
            val args = Bundle()
            args.putParcelable("application_info", packageName)
            args.putString("transition_name", transitionName)
            val fragment = AppInfo()
            fragment.arguments = args
            return fragment
        }
    }
}
