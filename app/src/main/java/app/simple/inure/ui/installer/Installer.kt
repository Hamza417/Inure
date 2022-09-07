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
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import app.simple.inure.R
import app.simple.inure.adapters.installer.AdapterInstallerInfoPanels
import app.simple.inure.adapters.menus.AdapterTabLayout
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.constants.BundleConstants
import app.simple.inure.constants.ServiceConstants
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.tablayout.SmartTabLayout
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.factories.installer.InstallerViewModelFactory
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.themes.manager.ThemeManager
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
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: SmartTabLayout

    private lateinit var broadcastReceiver: BroadcastReceiver
    private val intentFilter = IntentFilter()

    private var adapterTabLayout: AdapterTabLayout? = null

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

                    }
                    PackageInstaller.STATUS_FAILURE_ABORTED -> {

                    }
                    PackageInstaller.STATUS_FAILURE_BLOCKED -> {

                    }
                    PackageInstaller.STATUS_FAILURE_CONFLICT -> {

                    }
                    PackageInstaller.STATUS_FAILURE_INCOMPATIBLE -> {

                    }
                    PackageInstaller.STATUS_FAILURE_INVALID -> {

                    }
                    PackageInstaller.STATUS_FAILURE_STORAGE -> {

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
                installerViewModel.install()
            }

            update.setOnClickListener {
                installerViewModel.install()
            }

            uninstall.setOnClickListener {
                Toast.makeText(requireContext(), "Not yet implemented", Toast.LENGTH_SHORT).show()
            }
        }

        installerViewModel.getFile().observe(viewLifecycleOwner) {
            icon.loadAppIcon(it)

            val titles = arrayOf(getString(R.string.information), getString(R.string.permissions), getString(R.string.manifest), getString(R.string.services), getString(R.string.activities), getString(R.string.certificate))

            viewPager.adapter = AdapterInstallerInfoPanels(this, it, titles)
            tabLayout.setViewPager2(viewPager)
            viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrollStateChanged(state: Int) {
                    super.onPageScrollStateChanged(state)
                    if (state == ViewPager2.SCROLL_STATE_IDLE) {
                        handler.postDelayed({ adapterTabLayout?.layoutPositionChanged(viewPager.currentItem) }, 250)
                    }
                }
            })
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
        requireActivity().finish()
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