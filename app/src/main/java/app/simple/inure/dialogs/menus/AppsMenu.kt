package app.simple.inure.dialogs.menus

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.launchThisPackage
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.dialogs.action.Preparing
import app.simple.inure.dialogs.miscellaneous.Error
import app.simple.inure.extension.fragments.ScopedDialogFragment
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.preferences.BehaviourPreferences
import app.simple.inure.preferences.DevelopmentPreferences
import app.simple.inure.ui.panels.NotesEditor
import app.simple.inure.ui.viewers.*
import app.simple.inure.util.ConditionUtils.isNotZero
import app.simple.inure.util.FragmentHelper
import app.simple.inure.util.StatusBarHeight
import app.simple.inure.util.ViewUtils
import app.simple.inure.viewmodels.panels.QuickAppsViewModel

class AppsMenu : ScopedDialogFragment() {

    private lateinit var icon: ImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView

    private lateinit var copyPackageName: DynamicRippleTextView
    private lateinit var launch: DynamicRippleTextView
    private lateinit var appInformation: DynamicRippleTextView
    private lateinit var send: DynamicRippleTextView
    private lateinit var permissions: DynamicRippleTextView
    private lateinit var activities: DynamicRippleTextView
    private lateinit var services: DynamicRippleTextView
    private lateinit var receivers: DynamicRippleTextView
    private lateinit var providers: DynamicRippleTextView
    private lateinit var trackers: DynamicRippleTextView
    private lateinit var manifest: DynamicRippleTextView
    private lateinit var notes: DynamicRippleTextView
    private lateinit var toQuickApp: DynamicRippleTextView

    private lateinit var quickAppsViewModel: QuickAppsViewModel
    private var isAlreadyInQuickApp = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_apps_menu, container, false)

        icon = view.findViewById(R.id.fragment_app_info_icon)
        name = view.findViewById(R.id.fragment_app_name)
        packageName = view.findViewById(R.id.fragment_app_package_id)

        copyPackageName = view.findViewById(R.id.copy_package_name)
        launch = view.findViewById(R.id.launch)
        appInformation = view.findViewById(R.id.app_information)
        send = view.findViewById(R.id.send)
        permissions = view.findViewById(R.id.permissions)
        activities = view.findViewById(R.id.activities)
        services = view.findViewById(R.id.services)
        receivers = view.findViewById(R.id.receivers)
        providers = view.findViewById(R.id.providers)
        trackers = view.findViewById(R.id.trackers)
        manifest = view.findViewById(R.id.manifest)
        notes = view.findViewById(R.id.notes)
        toQuickApp = view.findViewById(R.id.to_quick_app)

        quickAppsViewModel = ViewModelProvider(requireActivity())[QuickAppsViewModel::class.java]
        packageInfo = requireArguments().getParcelable(BundleConstants.packageInfo)!!

        (parentFragment as ScopedFragment).clearExitTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val window = dialog!!.window ?: return
        val displayMetrics = DisplayMetrics()

        @Suppress("deprecation")
        window.windowManager.defaultDisplay.getMetrics(displayMetrics)

        if (BehaviourPreferences.isDimmingOn()) {
            dialog?.window?.setDimAmount(ViewUtils.getDimValue(requireContext()))
        } else {
            dialog?.window?.setDimAmount(0f)
        }

        window.attributes.gravity = Gravity.CENTER

        if (StatusBarHeight.isLandscape(requireContext())) {
            window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 60f).toInt()
            window.attributes.height = (displayMetrics.heightPixels * 1F / 100F * 90F).toInt()
        } else {
            window.attributes.width = (displayMetrics.widthPixels * 1f / 100f * 85f).toInt()
            window.attributes.height = (displayMetrics.heightPixels * 1F / 100F * 60F).toInt()
        }

        icon.loadAppIcon(packageInfo.packageName)

        name.text = packageInfo.applicationInfo.name
        packageName.text = packageInfo.packageName

        copyPackageName.setOnClickListener {
            val clipBoard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Package Name", packageInfo.packageName)
            clipBoard.setPrimaryClip(clipData)
            Toast.makeText(requireContext(), R.string.copied, Toast.LENGTH_SHORT).show()
            dismiss()
        }

        launch.setOnClickListener {
            kotlin.runCatching {
                packageInfo.launchThisPackage(requireContext())
            }.onFailure {
                val e = Error.newInstance(it.stackTraceToString())
                e.show(childFragmentManager, "error_dialog")
                e.setOnErrorDialogCallbackListener(object : Error.Companion.ErrorDialogCallbacks {
                    override fun onDismiss() {
                        dismiss()
                    }
                })
            }
        }

        appInformation.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Information.newInstance(packageInfo),
                                        "information")
        }

        send.setOnClickListener {
            Preparing.newInstance(packageInfo)
                .show(childFragmentManager, "send")
        }

        permissions.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Permissions.newInstance(packageInfo),
                                        "permissions")
        }

        activities.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Activities.newInstance(packageInfo),
                                        "activities")
        }

        services.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Services.newInstance(packageInfo),
                                        "services")
        }

        receivers.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Receivers.newInstance(packageInfo),
                                        "receivers")
        }

        providers.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Providers.newInstance(packageInfo),
                                        "providers")
        }

        trackers.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        Trackers.newInstance(packageInfo),
                                        "trackers")
        }

        manifest.setOnClickListener {
            if (DevelopmentPreferences.isWebViewXmlViewer()) {
                FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                            XMLViewerWebView.newInstance(packageInfo, true, "AndroidManifest.xml"),
                                            "xml")
            } else {
                FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                            XMLViewerTextView.newInstance(packageInfo, true, "AndroidManifest.xml"),
                                            "xml")
            }
        }

        notes.setOnClickListener {
            FragmentHelper.openFragment(requireActivity().supportFragmentManager,
                                        NotesEditor.newInstance(packageInfo),
                                        "notes_editor")
        }

        quickAppsViewModel.getSimpleQuickAppList().observe(viewLifecycleOwner) {
            if (it.size.isNotZero()) {
                for (i in it) {
                    if (i.packageName == packageInfo.packageName) {
                        toQuickApp.setText(R.string.remove_from_quick_apps)
                        isAlreadyInQuickApp = true
                        break
                    } else {
                        isAlreadyInQuickApp = false
                    }
                }
            } else {
                isAlreadyInQuickApp = false
            }

            if (!isAlreadyInQuickApp) {
                toQuickApp.setText(R.string.add_to_quick_apps)
                isAlreadyInQuickApp = false
            }
        }

        toQuickApp.setOnClickListener {
            if (isAlreadyInQuickApp) {
                quickAppsViewModel.removeQuickApp(packageInfo.packageName)
            } else {
                quickAppsViewModel.addQuickApp(packageInfo.packageName)
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo): AppsMenu {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            val fragment = AppsMenu()
            fragment.arguments = args
            return fragment
        }
    }
}