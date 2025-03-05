package app.simple.inure.dialogs.configuration

import android.content.SharedPreferences
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
import app.simple.inure.decorations.toggles.CheckBox
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ConditionUtils.invert
import app.simple.inure.util.SDCard
import app.simple.inure.util.StringUtils.containsAny

class AppPath : ScopedDialogFragment() {

    private lateinit var editText: DynamicCornerEditText
    private lateinit var pathInfo: TypeFaceTextView
    private lateinit var sdcardCheckbox: CheckBox
    private lateinit var save: DynamicRippleTextView
    private lateinit var close: DynamicRippleTextView
    private lateinit var reset: DynamicRippleTextView

    // |\\?*<\":>+[]/'"
    private val reservedCharactersAndroid =
        charArrayOf('|', '\\', '?', '*', '<', '\"', ':', '>', '+', '[', ']', '/', '\'')

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_app_path, container, false)

        editText = view.findViewById(R.id.path_edit_text)
        pathInfo = view.findViewById(R.id.path_info)
        sdcardCheckbox = view.findViewById(R.id.sdcard_checkbox)
        save = view.findViewById(R.id.save)
        close = view.findViewById(R.id.close)
        reset = view.findViewById(R.id.path_default)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        pathInfo.text = PackageData.getPackageDir(requireContext(), ConfigurationPreferences.getAppPath())?.absolutePath
        editText.setText(ConfigurationPreferences.getAppPath())
        sdcardCheckbox.isChecked = ConfigurationPreferences.isExternalStorage()

        editText.doOnTextChanged { text, _, _, _ ->
            pathInfo.text = PackageData.getPackageDir(requireContext(), text.toString())?.absolutePath

            if (text.toString().isValidPath().invert()) {
                pathInfo.error = "Invalid path: ${pathInfo.text} "
            } else {
                pathInfo.error = null
            }
        }

        save.setOnClickListener {
            kotlin.runCatching {
                if (editText.text.toString().isValidPath()) {
                    PackageData.makePackageFolder(requireContext(), editText.text.toString())
                    ConfigurationPreferences.setAppPath(editText.text.toString())
                    dismiss()
                } else {
                    showWarning("Invalid path: ${pathInfo.text} ", dismiss = false)
                }
            }.onFailure {
                pathInfo.error = it.localizedMessage
                showWarning("ERR: ${it.message ?: it.javaClass.name ?: "unknown error"} ", dismiss = false)
            }
        }

        sdcardCheckbox.setOnCheckedChangeListener {
            if (it) {
                if (SDCard.findSdCardPath(requireContext()) != null) {
                    ConfigurationPreferences.setExternalStorage(true)
                } else {
                    sdcardCheckbox.isChecked = false
                    showWarning("No SD Card found", dismiss = false)
                }
            } else {
                ConfigurationPreferences.setExternalStorage(false)
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

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        super.onSharedPreferenceChanged(sharedPreferences, key)
        when (key) {
            ConfigurationPreferences.IS_EXTERNAL_STORAGE -> {
                kotlin.runCatching {
                    pathInfo.text = PackageData.getPackageDir(requireContext(), editText.text.toString())?.absolutePath
                }.onFailure {
                    it.printStackTrace()
                    ConfigurationPreferences.setExternalStorage(false)
                }
            }
        }
    }

    private fun String.isValidPath(): Boolean {
        val packageDir = PackageData.getPackageDir(requireContext(), this)
        return (packageDir?.exists() == true && packageDir.isDirectory)
                || !containsAny(reservedCharactersAndroid)
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
            fragment.show(this, TAG)
            return fragment
        }

        const val TAG = "app_path"
    }
}
