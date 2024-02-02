package app.simple.inure.dialogs.debloat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class UninstallMethodChoice : ScopedBottomSheetFragment() {

    private lateinit var uninstall: DynamicRippleTextView
    private lateinit var disable: DynamicRippleTextView

    var onUninstallMethodSelected: ((Boolean) -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_uninstall_method_choice, container, false)

        uninstall = view.findViewById(R.id.uninstall)
        disable = view.findViewById(R.id.disable)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        uninstall.setOnClickListener {
            onUninstallMethodSelected?.invoke(true)
            dismiss()
        }

        disable.setOnClickListener {
            onUninstallMethodSelected?.invoke(false)
            dismiss()
        }
    }

    companion object {
        fun newInstance(): UninstallMethodChoice {
            val args = Bundle()
            val fragment = UninstallMethodChoice()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showUninstallMethodChoice(): UninstallMethodChoice {
            val dialog = newInstance()
            dialog.show(this, "uninstall_method_choice")
            return dialog
        }
    }
}