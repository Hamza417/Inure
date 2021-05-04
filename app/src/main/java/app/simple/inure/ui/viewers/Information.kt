package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.getDexData
import app.simple.inure.util.APKParser.getGlEsVersion
import app.simple.inure.util.APKParser.getInstallLocation
import app.simple.inure.util.NullSafety.isNull
import app.simple.inure.util.PackageUtils
import app.simple.inure.util.PackageUtils.getApplicationInstallTime
import app.simple.inure.util.PackageUtils.getApplicationLastUpdateTime
import app.simple.inure.util.SDKHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.NumberFormat

class Information : ScopedFragment() {

    private lateinit var version: TypeFaceTextView
    private lateinit var versionCode: TypeFaceTextView
    private lateinit var installLocation: TypeFaceTextView
    private lateinit var minSdk: TypeFaceTextView
    private lateinit var targetSdk: TypeFaceTextView
    private lateinit var glesVersion: TypeFaceTextView
    private lateinit var uid: TypeFaceTextView
    private lateinit var installDate: TypeFaceTextView
    private lateinit var updateDate: TypeFaceTextView
    private lateinit var methodCount: TypeFaceTextView
    private lateinit var back: DynamicRippleImageButton

    private lateinit var applicationInfo: ApplicationInfo

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = layoutInflater.inflate(R.layout.fragment_information, container, false)

        version = view.findViewById(R.id.sub_information_version)
        versionCode = view.findViewById(R.id.sub_information_version_code)
        installLocation = view.findViewById(R.id.sub_information_install_location)
        minSdk = view.findViewById(R.id.sub_information_min_sdk)
        targetSdk = view.findViewById(R.id.sub_information_target_sdk)
        glesVersion = view.findViewById(R.id.sub_information_gles)
        uid = view.findViewById(R.id.sub_information_uid)
        installDate = view.findViewById(R.id.sub_information_install_date)
        updateDate = view.findViewById(R.id.sub_information_update_date)
        methodCount = view.findViewById(R.id.sub_information_method_count)
        back = view.findViewById(R.id.app_info_back_button)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            "${applicationInfo.minSdkVersion}, ${SDKHelper.getSdkTitle(applicationInfo.minSdkVersion)}".also { minSdk.text = it }
        }
        "${applicationInfo.targetSdkVersion}, ${SDKHelper.getSdkTitle(applicationInfo.targetSdkVersion)}".also { targetSdk.text = it }

        launch {
            var version: String
            var versionCode: String
            var installLocation: String
            var glesVersion: String
            var uid: String
            var installDate: String
            var updateDate: String
            var methodCount: Int = -1
            var isMultiDex: Boolean

            withContext(Dispatchers.Default) {
                version = PackageUtils.getApplicationVersion(requireContext(), applicationInfo)
                versionCode = PackageUtils.getApplicationVersionCode(requireContext(), applicationInfo)
                installLocation = try {
                    applicationInfo.getInstallLocation()
                } catch (e: NullPointerException) {
                    getString(R.string.not_available)
                }
                glesVersion = if (applicationInfo.getGlEsVersion().isNull()) getString(R.string.not_available) else applicationInfo.getGlEsVersion().toString()
                uid = applicationInfo.uid.toString()
                installDate = applicationInfo.getApplicationInstallTime(requireContext())
                updateDate = applicationInfo.getApplicationLastUpdateTime(requireContext())

                val v = applicationInfo.getDexData()!!

                for (i in v) {
                    methodCount += i.header.methodIdsSize
                }

                isMultiDex = v.size > 1
            }

            this@Information.version.text = version
            this@Information.versionCode.text = versionCode
            this@Information.installLocation.text = installLocation
            this@Information.glesVersion.text = glesVersion
            this@Information.uid.text = uid
            this@Information.installDate.text = installDate
            this@Information.updateDate.text = updateDate
            this@Information.methodCount.text = if (isMultiDex) {
                String.format(getString(R.string.multi_dex), NumberFormat.getNumberInstance().format(methodCount))
            } else {
                String.format(getString(R.string.single_dex), NumberFormat.getNumberInstance().format(methodCount))
            }
        }

        back.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Information {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Information()
            fragment.arguments = args
            return fragment
        }
    }
}