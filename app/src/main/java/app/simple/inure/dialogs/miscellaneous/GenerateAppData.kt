package app.simple.inure.dialogs.miscellaneous

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.checkbox.InureCheckBox
import app.simple.inure.decorations.ripple.DynamicRippleLinearLayout
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.dialog.GeneratedDataCallbacks
import app.simple.inure.popups.app.PopupGeneratedDataFormat
import app.simple.inure.preferences.GeneratedDataPreferences

class GenerateAppData : ScopedBottomSheetFragment() {

    private lateinit var name: DynamicRippleLinearLayout
    private lateinit var packageName: DynamicRippleLinearLayout
    private lateinit var version: DynamicRippleLinearLayout
    private lateinit var installDate: DynamicRippleLinearLayout
    private lateinit var updateDate: DynamicRippleLinearLayout
    private lateinit var playStore: DynamicRippleLinearLayout
    private lateinit var fdroid: DynamicRippleLinearLayout

    private lateinit var nameCheckBox: InureCheckBox
    private lateinit var packageNameCheckBox: InureCheckBox
    private lateinit var versionCheckBox: InureCheckBox
    private lateinit var installDateCheckBox: InureCheckBox
    private lateinit var updateDateCheckBox: InureCheckBox
    private lateinit var playStoreCheckBox: InureCheckBox
    private lateinit var fdroidCheckBox: InureCheckBox

    private lateinit var format: DynamicRippleTextView
    private lateinit var generate: DynamicRippleTextView

    private var generatedDataCallbacks: GeneratedDataCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_generated_data_type, container, false)

        name = view.findViewById(R.id.name)
        packageName = view.findViewById(R.id.package_name)
        version = view.findViewById(R.id.version)
        installDate = view.findViewById(R.id.install_date)
        updateDate = view.findViewById(R.id.update_date)
        playStore = view.findViewById(R.id.play_store)
        fdroid = view.findViewById(R.id.fdroid)

        nameCheckBox = view.findViewById(R.id.name_checkbox)
        packageNameCheckBox = view.findViewById(R.id.package_name_checkbox)
        versionCheckBox = view.findViewById(R.id.version_checkbox)
        installDateCheckBox = view.findViewById(R.id.install_date_checkbox)
        updateDateCheckBox = view.findViewById(R.id.update_date_checkbox)
        playStoreCheckBox = view.findViewById(R.id.play_store_checkbox)
        fdroidCheckBox = view.findViewById(R.id.fdroid_checkbox)

        format = view.findViewById(R.id.format)
        generate = view.findViewById(R.id.generate)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setDataFormat()
        nameCheckBox.setChecked(GeneratedDataPreferences.isGeneratedName())
        packageNameCheckBox.setChecked(GeneratedDataPreferences.isGeneratedPackageName())
        versionCheckBox.setChecked(GeneratedDataPreferences.isGeneratedVersion())
        installDateCheckBox.setChecked(GeneratedDataPreferences.isGeneratedInstallDate())
        updateDateCheckBox.setChecked(GeneratedDataPreferences.isGeneratedUpdateDate())
        playStoreCheckBox.setChecked(GeneratedDataPreferences.isGeneratedPlayStore())
        fdroidCheckBox.setChecked(GeneratedDataPreferences.isGeneratedFdroid())
        generateButtonState()

        name.setOnClickListener {
            nameCheckBox.toggle()
        }

        packageName.setOnClickListener {
            packageNameCheckBox.toggle()
        }

        version.setOnClickListener {
            versionCheckBox.toggle()
        }

        installDate.setOnClickListener {
            installDateCheckBox.toggle()
        }

        updateDate.setOnClickListener {
            updateDateCheckBox.toggle()
        }

        playStore.setOnClickListener {
            playStoreCheckBox.toggle()
        }

        fdroid.setOnClickListener {
            fdroidCheckBox.toggle()
        }

        nameCheckBox.setOnCheckedChangeListener { isChecked ->
            GeneratedDataPreferences.setGeneratedName(isChecked)
        }

        packageNameCheckBox.setOnCheckedChangeListener { isChecked ->
            GeneratedDataPreferences.setGeneratedPackageName(isChecked)
        }

        versionCheckBox.setOnCheckedChangeListener { isChecked ->
            GeneratedDataPreferences.setGeneratedVersion(isChecked)
        }

        installDateCheckBox.setOnCheckedChangeListener { isChecked ->
            GeneratedDataPreferences.setGeneratedInstallDate(isChecked)
        }

        updateDateCheckBox.setOnCheckedChangeListener { isChecked ->
            GeneratedDataPreferences.setGeneratedUpdateDate(isChecked)
        }

        playStoreCheckBox.setOnCheckedChangeListener { isChecked ->
            GeneratedDataPreferences.setGeneratedPlayStore(isChecked)
        }

        fdroidCheckBox.setOnCheckedChangeListener { isChecked ->
            GeneratedDataPreferences.setGeneratedFdroid(isChecked)
        }

        format.setOnClickListener {
            PopupGeneratedDataFormat(it)
        }

        generate.setOnClickListener {
            generatedDataCallbacks?.onGenerateData()
            dismiss()
        }
    }

    private fun setDataFormat() {
        format.text = when (GeneratedDataPreferences.getGeneratedDataType()) {
            GeneratedDataPreferences.TXT -> getString(R.string.txt)
            GeneratedDataPreferences.MD -> getString(R.string.markdown)
            GeneratedDataPreferences.HTML -> getString(R.string.html)
            GeneratedDataPreferences.CSV -> getString(R.string.csv)
            GeneratedDataPreferences.XML -> getString(R.string.xml)
            GeneratedDataPreferences.JSON -> getString(R.string.json)
            else -> getString(R.string.unknown)
        }
    }

    private fun isAtLeastOneIdFieldIsSelected(): Boolean {
        return GeneratedDataPreferences.isGeneratedName() || GeneratedDataPreferences.isGeneratedPackageName()
    }

    private fun generateButtonState() {
        if (isAtLeastOneIdFieldIsSelected()) {
            Log.d("GenerateAppData", "generateButtonState: True ${isAtLeastOneIdFieldIsSelected()}")
            generate.isEnabled = true
            generate.isClickable = true
            generate.animate().alpha(1f).setDuration(300).start()
        } else {
            Log.d("GenerateAppData", "generateButtonState: False ${isAtLeastOneIdFieldIsSelected()}")
            generate.isEnabled = false
            generate.isClickable = false
            generate.animate().alpha(0.5f).setDuration(300).start()
        }
    }

    fun onGenerateData(generatedDataCallbacks: GeneratedDataCallbacks) {
        this.generatedDataCallbacks = generatedDataCallbacks
    }

    override fun onDestroy() {
        generate.clearAnimation()
        super.onDestroy()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            GeneratedDataPreferences.generatedDataType -> {
                setDataFormat()
            }
            GeneratedDataPreferences.name, GeneratedDataPreferences.packageName -> {
                generateButtonState()
            }
        }
    }

    companion object {
        fun newInstance(): GenerateAppData {
            return GenerateAppData()
        }

        fun FragmentManager.showGeneratedDataTypeSelector(): GenerateAppData {
            val fragment = newInstance()
            fragment.show(this, "generated_data_type")
            return fragment
        }
    }
}