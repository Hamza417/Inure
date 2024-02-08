package app.simple.inure.dialogs.app

import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.util.StringUtils.emptyString
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.autheticators.GumroadLicenceAuthenticatorViewModel

class LicenseKey : ScopedDialogFragment() {

    private lateinit var editText: DynamicCornerEditText
    private lateinit var info: TypeFaceTextView
    private lateinit var verify: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var gumroadLicenceAuthenticatorViewModel: GumroadLicenceAuthenticatorViewModel? = null
    private var inputFilter: InputFilter? = null

    var pattern = "0123456789ABCDEF-" // The pattern for the licence key

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_license_key, container, false)

        editText = view.findViewById(R.id.edit_text)
        info = view.findViewById(R.id.info)
        verify = view.findViewById(R.id.verify)
        cancel = view.findViewById(R.id.cancel)

        verify.gone()
        info.gone()

        gumroadLicenceAuthenticatorViewModel = ViewModelProvider(this)[GumroadLicenceAuthenticatorViewModel::class.java]

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        inputFilter = InputFilter { source, _, _, _, _, _ ->
            val string = source.toString()
            for (element in string) {
                if (!pattern.contains(element)) {
                    return@InputFilter emptyString()
                }
            }

            source
        }

        editText.doOnTextChanged { text, _, _, _ ->
            if (text.toString().length == 35) {
                verify.visible(false)
            } else {
                verify.gone()
            }
        }

        editText.filters = arrayOf(inputFilter)

        verify.setOnClickListener {
            gumroadLicenceAuthenticatorViewModel?.verifyLicence(editText.text.toString())
        }

        cancel.setOnClickListener {
            dismiss()
        }

        gumroadLicenceAuthenticatorViewModel?.getLicenseStatus()?.observe(viewLifecycleOwner) {
            if (it) {
                Log.d("LicenseKey", "Licence is valid")
            } else {
                Log.d("LicenseKey", "Licence is invalid")
            }
        }

        gumroadLicenceAuthenticatorViewModel?.getMessage()?.observe(viewLifecycleOwner) {
            info.text = it
            info.visible(true)
        }
    }

    companion object {
        fun newInstance(): LicenseKey {
            val args = Bundle()
            val fragment = LicenseKey()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showLicenseKey() {
            val dialog = newInstance()
            dialog.show(this, "license_key")
        }
    }
}