package app.simple.inure.ui.installer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.R
import app.simple.inure.adapters.installer.AdapterInstallerInfoPanels
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.tablayout.SmartTabLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.dialogs.action.Uninstaller
import app.simple.inure.dialogs.app.Sure
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.interfaces.fragments.SureCallbacks
import app.simple.inure.themes.manager.ThemeManager
import app.simple.inure.util.FileUtils.findFile
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.installer.InstallerViewModel

class Installer : ScopedFragment() {

    private lateinit var installerViewModel: InstallerViewModel

    private lateinit var icon: AppIconImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageName: TypeFaceTextView
    private lateinit var install: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView
    private lateinit var update: DynamicRippleTextView
    private lateinit var uninstall: DynamicRippleTextView
    private lateinit var loader: CustomProgressBar
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: SmartTabLayout

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val intentFilter = IntentFilter()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_installer, container, false)

        icon = view.findViewById(R.id.icon)
        name = view.findViewById(R.id.name)
        packageName = view.findViewById(R.id.package_id)
        install = view.findViewById(R.id.install)
        cancel = view.findViewById(R.id.cancel)
        update = view.findViewById(R.id.update)
        uninstall = view.findViewById(R.id.uninstall)
        viewPager = view.findViewById(R.id.viewPager)
        tabLayout = view.findViewById(R.id.tabLayout)
        loader = view.findViewById(R.id.loader)

        val factory = InstallerViewModelFactory(requireArguments().getParcelable(BundleConstants.uri)!!)
        installerViewModel = ViewModelProvider(this, factory)[InstallerViewModel::class.java]

        intentFilter.addAction(ServiceConstants.actionSessionStatus)
        viewPager.offscreenPageLimit = 5

        tabLayout.apply {
            setDefaultTabTextColor(ColorStateList.valueOf(ThemeManager.theme.textViewTheme.secondaryTextColor))
            setSelectedIndicatorColors(ThemeManager.theme.viewGroupTheme.selectedBackground)
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                when (intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -999)) {
                    PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                        val confirmationIntent = intent.getParcelableExtra<Intent>(Intent.EXTRA_INTENT)
                        if (confirmationIntent != null) {
                            confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            try {
                                context.startActivity(confirmationIntent)
                            } catch (ignored: Exception) {
                            }
                        }
                    }
                    PackageInstaller.STATUS_SUCCESS -> {
                        update.gone()
                        install.gone()
                        loader.gone()
                        uninstall.visible(false)
                        cancel.setText(R.string.close)
                    }
                    PackageInstaller.STATUS_FAILURE_ABORTED -> {
                        showWarning(intent.extras!!.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)!!)
                    }
                    PackageInstaller.STATUS_FAILURE_BLOCKED,
                    PackageInstaller.STATUS_FAILURE_CONFLICT -> {
                        showWarning(intent.extras!!.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)!! +
                                            " -> " +
                                            intent.extras!!.getString(PackageInstaller.EXTRA_PACKAGE_NAME)!!)
                    }
                    PackageInstaller.STATUS_FAILURE_INCOMPATIBLE,
                    PackageInstaller.STATUS_FAILURE_INVALID,
                    PackageInstaller.STATUS_FAILURE_STORAGE -> {
                        showWarning(intent.extras!!.getString(PackageInstaller.EXTRA_STATUS_MESSAGE)!!)
                    }
                    else -> {

                    }
                }
            }
        }

        installerViewModel.getPackageInfo().observe(viewLifecycleOwner) {
            packageInfo = it

            name.text = packageInfo.applicationInfo.name
            packageName.text = buildString {
                append(packageInfo.packageName)
                append(" (${packageInfo.versionName})")
            }

            if (PackageUtils.isPackageInstalled(packageInfo.packageName, requireContext().packageManager)) {
                install.gone()
                update.visible(true)
                uninstall.visible(true)
            } else {
                install.visible(true)
                update.gone()
                uninstall.gone()
            }

            install.setOnClickListener {
                loader.visible(true)
                installerViewModel.install()
            }

            update.setOnClickListener {
                install.callOnClick()
            }

            uninstall.setOnClickListener {
                val sure = Sure.newInstance()
                sure.setOnSureCallbackListener(object : SureCallbacks {
                    override fun onSure() {
                        val uninstaller = Uninstaller.newInstance(packageInfo)

                        uninstaller.listener = {
                            requireActivity().finish()
                        }

                        uninstaller.show(childFragmentManager, "uninstaller")
                    }
                })

                sure.show(childFragmentManager, "sure")
            }
        }

        installerViewModel.getFile().observe(viewLifecycleOwner) {
            val file = if (it.size > 1) it.findFile("base.apk")!! else it[0]
            icon.loadAppIcon(file)

            val titles = arrayOf(getString(R.string.information), getString(R.string.permissions), getString(R.string.manifest), getString(R.string.services), getString(R.string.activities), getString(R.string.certificate))

            viewPager.adapter = AdapterInstallerInfoPanels(this, file, titles)
            tabLayout.setViewPager2(viewPager)
        }

        cancel.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(broadcastReceiver)
    }

    companion object {
        fun newInstance(uri: Uri): Installer {
            val args = Bundle()
            args.putParcelable(BundleConstants.uri, uri)
            val fragment = Installer()
            fragment.arguments = args
            return fragment
        }
    }
}