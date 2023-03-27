package app.simple.inure.dialogs.configuration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageData
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.preferences.ConfigurationPreferences

class AppPath : ScopedDialogFragment() {

    private lateinit var editText: DynamicCornerEditText
    private lateinit var pathInfo: TypeFaceTextView
    private lateinit var save: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var reset: DynamicRippleTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_app_path, container, false)

        editText = view.findViewById(R.id.path_edit_text)
        pathInfo = view.findViewById(R.id.path_info)
        save = view.findViewById(R.id.save)
        close = view.findViewById(R.id.close)
        reset = view.findViewById(R.id.path_default)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pathInfo.text = PackageData.getPackageDir(requireContext(), ConfigurationPreferences.getAppPath())?.absolutePath
        editText.setText(ConfigurationPreferences.getAppPath())

        editText.doOnTextChanged { text, _, _, _ ->
            pathInfo.text = PackageData.getPackageDir(requireContext(), text.toString())?.absolutePath
        }

        save.setOnClickListener {
            kotlin.runCatching {
                PackageData.makePackageFolder(requireContext(), editText.text.toString())
                ConfigurationPreferences.setAppPath(editText.text.toString())
                dismiss()
            }.onFailure {
                pathInfo.error = it.localizedMessage
            }
        }

        reset.setOnClickListener {
            ConfigurationPreferences.defaultAppPath()
            editText.setText(ConfigurationPreferences.getAppPath())
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    companion object {
        fun newInstance(): AppPath {
            val args = Bundle()
            val fragment = AppPath()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAppPathDialog(): AppPath {
            val fragment = newInstance()
            fragment.show(this, "app_path")
            return fragment
        }
    }
}