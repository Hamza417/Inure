package app.simple.inure.ui.preferences.mainscreens

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import app.simple.inure.BuildConfig
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils.isPackageInstalledAndEnabled
import app.simple.inure.decorations.ripple.DynamicRippleRelativeLayout
import app.simple.inure.decorations.switchview.SwitchView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.glide.util.ImageLoader.loadAppIcon
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.ui.preferences.subscreens.Language
import app.simple.inure.ui.preferences.subscreens.Shortcuts
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ConfigurationScreen : ScopedFragment() {

    private lateinit var keepScreenOnSwitchView: SwitchView
    private lateinit var shortcuts: DynamicRippleRelativeLayout
    private lateinit var language: DynamicRippleRelativeLayout
    private lateinit var rootSwitchView: SwitchView
    private lateinit var shizukuSwitchView: SwitchView
    private lateinit var shizukuIcon: ImageView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.preferences_configuration, container, false)

        keepScreenOnSwitchView = view.findViewById(R.id.configuration_switch_keep_screen_on)
        shortcuts = view.findViewById(R.id.configuration_shortcuts)
        language = view.findViewById(R.id.configuration_language)
        rootSwitchView = view.findViewById(R.id.configuration_root_switch_view)
        shizukuIcon = view.findViewById(R.id.shizuku_icon)
        shizukuSwitchView = view.findViewById(R.id.configuration_shizuku_switch_view)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        keepScreenOnSwitchView.setChecked(ConfigurationPreferences.isKeepScreenOn())
        rootSwitchView.setChecked(ConfigurationPreferences.isUsingRoot())
        shizukuIcon.loadAppIcon("moe.shizuku.privileged.api", requirePackageManager().isPackageInstalledAndEnabled("moe.shizuku.privileged.api"))

        keepScreenOnSwitchView.setOnSwitchCheckedChangeListener { isChecked ->
            ConfigurationPreferences.setKeepScreenOn(isChecked)

            if (isChecked) {
                requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        shortcuts.setOnClickListener {
            openFragmentSlide(Shortcuts.newInstance(), "shortcuts")
        }

        language.setOnClickListener {
            openFragmentSlide(Language.newInstance(), "language")
        }

        rootSwitchView.setOnSwitchCheckedChangeListener { it ->
            if (it) {
                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                    kotlin.runCatching {
                        Shell.enableVerboseLogging = BuildConfig.DEBUG
                        Shell.setDefaultBuilder(
                                Shell.Builder
                                    .create()
                                    .setFlags(Shell.FLAG_REDIRECT_STDERR or Shell.FLAG_MOUNT_MASTER)
                                    .setTimeout(10))
                    }.getOrElse {
                        it.printStackTrace()
                    }

                    Shell.getShell() // Request root access

                    if (Shell.isAppGrantedRoot() == true) {
                        withContext(Dispatchers.Main) {
                            ConfigurationPreferences.setUsingRoot(true)
                            rootSwitchView.setChecked(true)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            ConfigurationPreferences.setUsingRoot(false)
                            rootSwitchView.setChecked(false)
                        }
                    }
                }
            } else {
                ConfigurationPreferences.setUsingRoot(false)
                rootSwitchView.setChecked(false)
            }
        }

        shizukuSwitchView.setOnSwitchCheckedChangeListener { it ->

        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            ConfigurationPreferences.isUsingRoot -> {
                // rootSwitchView.setChecked(ConfigurationPreferences.isUsingRoot())
            }
        }
    }

    companion object {
        fun newInstance(): ConfigurationScreen {
            val args = Bundle()
            val fragment = ConfigurationScreen()
            fragment.arguments = args
            return fragment
        }
    }
}