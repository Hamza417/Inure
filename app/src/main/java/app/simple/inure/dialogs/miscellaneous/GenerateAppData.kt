package app.simple.inure.dialogs.miscellaneous

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.dialog.GeneratedDataCallbacks
import app.simple.inure.popups.app.PopupGeneratedDataFormat
import app.simple.inure.preferences.GeneratedDataPreferences
import app.simple.inure.util.AppUtils
import app.simple.inure.util.FlagUtils
import com.google.android.material.chip.ChipGroup

class GenerateAppData : ScopedBottomSheetFragment() {

    private lateinit var requiredChipGroup: ChipGroup
    private lateinit var optionalChipGroup: ChipGroup
    private lateinit var linkChipGroup: ChipGroup

    private lateinit var format: DynamicRippleTextView
    private lateinit var generate: DynamicRippleTextView

    private var generatedDataCallbacks: GeneratedDataCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_generated_data_type, container, false)

        requiredChipGroup = view.findViewById(R.id.required_chip_group)
        optionalChipGroup = view.findViewById(R.id.optional_chip_group)
        linkChipGroup = view.findViewById(R.id.link_chip_group)

        format = view.findViewById(R.id.format)
        generate = view.findViewById(R.id.generate)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setFlags()
        setDataFormat()
        generateButtonState()

        if (AppUtils.isPlayFlavor()) {
            linkChipGroup.removeView(linkChipGroup.findViewById(R.id.fdroid))
            linkChipGroup.removeView(linkChipGroup.findViewById(R.id.izzyondroid))
        }

        requiredChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var sourceFlags = GeneratedDataPreferences.getGeneratorFlags()

            sourceFlags = if (checkedIds.contains(R.id.name)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.NAME)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.NAME)
            }

            sourceFlags = if (checkedIds.contains(R.id.package_name)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.PACKAGE_NAME)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.PACKAGE_NAME)
            }

            GeneratedDataPreferences.setGeneratorFlags(sourceFlags)
        }

        optionalChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var sourceFlags = GeneratedDataPreferences.getGeneratorFlags()

            sourceFlags = if (checkedIds.contains(R.id.version)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.VERSION)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.VERSION)
            }

            sourceFlags = if (checkedIds.contains(R.id.install_date)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.INSTALL_DATE)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.INSTALL_DATE)
            }

            sourceFlags = if (checkedIds.contains(R.id.update_date)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.UPDATE_DATE)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.UPDATE_DATE)
            }

            sourceFlags = if (checkedIds.contains(R.id.min_sdk)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.MINIMUM_SDK)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.MINIMUM_SDK)
            }

            sourceFlags = if (checkedIds.contains(R.id.target_sdk)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.TARGET_SDK)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.TARGET_SDK)
            }

            sourceFlags = if (checkedIds.contains(R.id.size)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.SIZE)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.SIZE)
            }

            GeneratedDataPreferences.setGeneratorFlags(sourceFlags)
        }

        linkChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            var sourceFlags = GeneratedDataPreferences.getGeneratorFlags()

            sourceFlags = if (checkedIds.contains(R.id.play_store)) {
                FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.PLAY_STORE)
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.PLAY_STORE)
            }

            if (AppUtils.isGithubFlavor() || AppUtils.isBetaFlavor()) {
                sourceFlags = if (checkedIds.contains(R.id.fdroid)) {
                    FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.FDROID)
                } else {
                    FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.FDROID)
                }

                sourceFlags = if (checkedIds.contains(R.id.izzyondroid)) {
                    FlagUtils.setFlag(sourceFlags, GeneratedDataPreferences.IZZYONDROID)
                } else {
                    FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.IZZYONDROID)
                }
            } else {
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.IZZYONDROID)
                FlagUtils.unsetFlag(sourceFlags, GeneratedDataPreferences.FDROID)
            }

            GeneratedDataPreferences.setGeneratorFlags(sourceFlags)
        }

        format.setOnClickListener {
            PopupGeneratedDataFormat(it)
        }

        generate.setOnClickListener {
            generatedDataCallbacks?.onGenerateData()
            dismiss()
        }
    }

    private fun setFlags() {
        val flags = GeneratedDataPreferences.getGeneratorFlags()

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.NAME)) {
            requiredChipGroup.check(R.id.name)
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PACKAGE_NAME)) {
            requiredChipGroup.check(R.id.package_name)
        }

        // ---------------------------------------------------------------------------- //

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.VERSION)) {
            optionalChipGroup.check(R.id.version)
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.INSTALL_DATE)) {
            optionalChipGroup.check(R.id.install_date)
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.UPDATE_DATE)) {
            optionalChipGroup.check(R.id.update_date)
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.MINIMUM_SDK)) {
            optionalChipGroup.check(R.id.min_sdk)
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.TARGET_SDK)) {
            optionalChipGroup.check(R.id.target_sdk)
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.SIZE)) {
            optionalChipGroup.check(R.id.size)
        }

        // ---------------------------------------------------------------------------- //

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.PLAY_STORE)) {
            linkChipGroup.check(R.id.play_store)
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.FDROID)) {
            linkChipGroup.check(R.id.fdroid)
        }

        if (FlagUtils.isFlagSet(flags, GeneratedDataPreferences.IZZYONDROID)) {
            linkChipGroup.check(R.id.izzyondroid)
        }
    }

    private fun setDataFormat() {
        format.setTextWithSlideAnimation(
                when (GeneratedDataPreferences.getGeneratedDataType()) {
                    GeneratedDataPreferences.TXT -> getString(R.string.txt)
                    GeneratedDataPreferences.MD -> getString(R.string.markdown)
                    GeneratedDataPreferences.HTML -> getString(R.string.html)
                    GeneratedDataPreferences.CSV -> getString(R.string.csv)
                    GeneratedDataPreferences.XML -> getString(R.string.xml)
                    GeneratedDataPreferences.JSON -> getString(R.string.json)
                    else -> getString(R.string.unknown)
                })
    }

    private fun isAtLeastOneIdFieldIsSelected(): Boolean {
        return FlagUtils.isFlagSet(GeneratedDataPreferences.getGeneratorFlags(), GeneratedDataPreferences.NAME) ||
                FlagUtils.isFlagSet(GeneratedDataPreferences.getGeneratorFlags(), GeneratedDataPreferences.PACKAGE_NAME)
    }

    private fun generateButtonState() {
        if (isAtLeastOneIdFieldIsSelected()) {
            generate.isEnabled = true
            generate.isClickable = true
            generate.animate().alpha(1f).setDuration(300).start()
        } else {
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
            GeneratedDataPreferences.GENERATED_DATA_TYPE -> {
                setDataFormat()
            }
        }
    }

    companion object {
        fun newInstance(): GenerateAppData {
            return GenerateAppData()
        }

        fun FragmentManager.showGeneratedDataTypeSelector(): GenerateAppData {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }

        const val TAG = "generated_data_type"
    }
}
