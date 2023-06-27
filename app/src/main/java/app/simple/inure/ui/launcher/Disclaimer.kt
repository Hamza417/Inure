package app.simple.inure.ui.launcher

import android.Manifest
import android.app.AppOpsManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.AppOpsManagerCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.preferences.MainPreferences
import app.simple.inure.util.HtmlHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Disclaimer : ScopedFragment() {

    private lateinit var txt: TypeFaceTextView
    private lateinit var agree: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_disclaimer, container, false)

        txt = view.findViewById(R.id.disclaimer)
        agree = view.findViewById(R.id.agree)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()

        // Set justifying text
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            txt.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
        }

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            requireContext().assets.open("txt/disclaimer.txt").bufferedReader().use {
                HtmlHelper.fromHtml(it.readText())
            }.let {
                requireActivity().runOnUiThread {
                    txt.text = it
                }
            }
        }

        agree.setOnClickListener {
            MainPreferences.setDisclaimerAgreed(true)

            if (checkForPermission()) {
                openFragmentSlide(SplashScreen.newInstance(skip = false))
            } else {
                openFragmentSlide(Setup.newInstance())
            }
        }
    }

    private fun checkForPermission(): Boolean {
        val appOps = requireContext().getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        } else {
            @Suppress("Deprecation")
            appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), requireContext().packageName)
        }

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mode == AppOpsManagerCompat.MODE_ALLOWED && Environment.isExternalStorageManager()
        } else {
            mode == AppOpsManagerCompat.MODE_ALLOWED &&
                    (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                            ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        }
    }

    companion object {
        fun newInstance(): Disclaimer {
            val args = Bundle()
            val fragment = Disclaimer()
            fragment.arguments = args
            return fragment
        }
    }
}