package app.simple.inure.ui.launcher

import android.app.AppOpsManager
import android.content.Context
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.core.app.AppOpsManagerCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.ui.app.Home
import app.simple.inure.util.FragmentHelper.openFragment
import app.simple.inure.viewmodels.panels.AllAppsData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreen : ScopedFragment() {

    private lateinit var icon: ImageView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash_screen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        icon = view.findViewById(R.id.imageView)
        icon.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.app_icon_animation))

        when {
            requireArguments().getBoolean("skip") -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000L) // Make sure the animation finishes
                    proceed()
                }
            }
            requireActivity().contentResolver.persistedUriPermissions.isNullOrEmpty() || !checkForPermission() -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000L) // Make sure the animation runs
                    openFragment(
                        requireActivity().supportFragmentManager,
                        Setup.newInstance(), view.findViewById(R.id.imageView))
                }
            }
            else -> {
                viewLifecycleOwner.lifecycleScope.launch {
                    delay(1000L) // Make sure the animation finishes
                    proceed()
                }
            }
        }
    }

    private fun proceed() {
        val allAppsData: AllAppsData by viewModels()

        allAppsData.getAppData().observe(viewLifecycleOwner, {
            openFragment(
                requireActivity().supportFragmentManager,
                Home.newInstance(), requireView().findViewById(R.id.imageView))
        })
    }

    private fun checkForPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        } else {
            @Suppress("Deprecation")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        }
        return mode == AppOpsManagerCompat.MODE_ALLOWED
    }

    companion object {
        fun newInstance(skip: Boolean): SplashScreen {
            val args = Bundle()
            args.putBoolean("skip", skip)
            val fragment = SplashScreen()
            fragment.arguments = args
            return fragment
        }
    }
}