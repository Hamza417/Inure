package app.simple.inure.dialogs.app

import android.animation.LayoutTransition
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.corners.DynamicCornerLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.AppUtils
import app.simple.inure.util.IntentHelper.asUri
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.MarketUtils
import app.simple.inure.util.StringUtils
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.autheticators.GumroadLicenceAuthenticatorViewModel

class Purchase : ScopedBottomSheetFragment() {

    private lateinit var close: DynamicRippleTextView
    private lateinit var licenseKey: DynamicCornerEditText
    private lateinit var verify: DynamicRippleTextView
    private lateinit var info: TypeFaceTextView
    private lateinit var playStore: DynamicRippleTextView
    private lateinit var gumroad: DynamicRippleTextView
    private lateinit var github: DynamicRippleTextView
    private lateinit var kofi: DynamicRippleTextView

    private var gumroadLicenceAuthenticatorViewModel: GumroadLicenceAuthenticatorViewModel? = null
    private var inputFilter: InputFilter? = null

    var pattern = "0123456789ABCDEF-" // The pattern for the licence key

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_purchase, container, false)

        licenseKey = view.findViewById(R.id.license_key)
        verify = view.findViewById(R.id.verify)
        info = view.findViewById(R.id.info)
        playStore = view.findViewById(R.id.play_store)
        gumroad = view.findViewById(R.id.gumroad)
        github = view.findViewById(R.id.github)
        kofi = view.findViewById(R.id.kofi)
        close = view.findViewById(R.id.close)

        verify.gone()
        info.gone()

        gumroadLicenceAuthenticatorViewModel = ViewModelProvider(this)[GumroadLicenceAuthenticatorViewModel::class.java]

        val transition = LayoutTransition()
        transition.setAnimateParentHierarchy(false)
        (view.findViewById<DynamicCornerLinearLayout>(R.id.dialog_purchase)).layoutTransition = transition
        (view.findViewById<LinearLayout>(R.id.license_key_container)).layoutTransition = transition

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gumroad.setOnClickListener {
            // Open GumRoad link in Browser
            getString(R.string.gumroad_link).asUri().openInBrowser(requireContext())
        }

        github.setOnClickListener {
            // Open GitHub link in Browser
            getString(R.string.github_link).asUri().openInBrowser(requireContext())
        }

        kofi.setOnClickListener {
            // Open Ko-Fi link in Browser
            getString(R.string.kofi_link).asUri().openInBrowser(requireContext())
        }

        playStore.setOnClickListener {
            // Open in Play Store
            MarketUtils.openAppOnPlayStore(requireContext(), AppUtils.unlockerPackageName)
        }

        close.setOnClickListener {
            dismiss()
        }

        inputFilter = InputFilter { source, _, _, _, _, _ ->
            val string = source.toString()
            for (element in string) {
                if (!pattern.contains(element)) {
                    return@InputFilter StringUtils.emptyString()
                }
            }

            source
        }

        licenseKey.doOnTextChanged { text, _, _, _ ->
            if (text.toString().length == 35) {
                verify.visible(false)
            } else {
                verify.gone()
            }
        }

        licenseKey.filters = arrayOf(inputFilter)

        verify.setOnClickListener {
            info.setText(R.string.verifying_license)
            info.visible(animate = true)
            gumroadLicenceAuthenticatorViewModel?.verifyLicence(licenseKey.text.toString())
        }

        gumroadLicenceAuthenticatorViewModel?.getLicenseStatus()?.observe(viewLifecycleOwner) {
            if (it) {
                Log.d("LicenseKey", "Licence is valid")
                handler.post {
                    dismiss()
                }
            } else {
                Log.d("LicenseKey", "Licence is invalid")
            }
        }

        gumroadLicenceAuthenticatorViewModel?.getMessage()?.observe(viewLifecycleOwner) {
            info.text = it
            info.visible(animate = true)
        }

        gumroadLicenceAuthenticatorViewModel?.getWarning()?.observe(viewLifecycleOwner) {
            info.text = it
            info.visible(animate = true)
        }
    }

    companion object {
        fun newInstance(): Purchase {
            val args = Bundle()
            val fragment = Purchase()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showPurchaseDialog() {
            newInstance().show(this, "purchase")
        }
    }
}