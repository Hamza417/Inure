package app.simple.inure.dialogs.tags

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.FlagUtils
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible
import com.google.android.material.chip.ChipGroup

class AutoTag : ScopedBottomSheetFragment() {

    private lateinit var tagsChipGroup: ChipGroup
    private lateinit var create: DynamicRippleTextView
    private lateinit var selectAll: DynamicRippleImageButton

    private var autoTagCallback: AutoTagCallback? = null

    private var storedTags: Long = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_auto_tag, container, false)

        tagsChipGroup = view.findViewById(R.id.tag_chip_group)
        create = view.findViewById(R.id.auto_tag)
        selectAll = view.findViewById(R.id.select_all)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        create.gone()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            tagsChipGroup.removeView(view.findViewById(R.id.accessibility))
        }

        tagsChipGroup.setOnCheckedStateChangeListener { _, ids ->
            storedTags = 0

            storedTags = if (ids.contains(R.id.game)) {
                FlagUtils.setFlag(storedTags, GAME)
            } else {
                FlagUtils.unsetFlag(storedTags, GAME)
            }

            storedTags = if (ids.contains(R.id.audio)) {
                FlagUtils.setFlag(storedTags, AUDIO)
            } else {
                FlagUtils.unsetFlag(storedTags, AUDIO)
            }

            storedTags = if (ids.contains(R.id.video)) {
                FlagUtils.setFlag(storedTags, VIDEO)
            } else {
                FlagUtils.unsetFlag(storedTags, VIDEO)
            }

            storedTags = if (ids.contains(R.id.image)) {
                FlagUtils.setFlag(storedTags, IMAGE)
            } else {
                FlagUtils.unsetFlag(storedTags, IMAGE)
            }

            storedTags = if (ids.contains(R.id.social)) {
                FlagUtils.setFlag(storedTags, SOCIAL)
            } else {
                FlagUtils.unsetFlag(storedTags, SOCIAL)
            }

            storedTags = if (ids.contains(R.id.news)) {
                FlagUtils.setFlag(storedTags, NEWS)
            } else {
                FlagUtils.unsetFlag(storedTags, NEWS)
            }

            storedTags = if (ids.contains(R.id.maps)) {
                FlagUtils.setFlag(storedTags, MAPS)
            } else {
                FlagUtils.unsetFlag(storedTags, MAPS)
            }

            storedTags = if (ids.contains(R.id.productivity)) {
                FlagUtils.setFlag(storedTags, PRODUCTIVITY)
            } else {
                FlagUtils.unsetFlag(storedTags, PRODUCTIVITY)
            }

            storedTags = if (ids.contains(R.id.xposed_module)) {
                FlagUtils.setFlag(storedTags, XPOSED_MODULE)
            } else {
                FlagUtils.unsetFlag(storedTags, XPOSED_MODULE)
            }

            storedTags = if (ids.contains(R.id.foss)) {
                FlagUtils.setFlag(storedTags, FOSS)
            } else {
                FlagUtils.unsetFlag(storedTags, FOSS)
            }

            when {
                storedTags != 0L -> {
                    create.visible()
                }
                else -> {
                    create.gone()
                }
            }
        }

        create.setOnClickListener {
            autoTagCallback?.onAutoTag(storedTags).also {
                dismiss()
            }
        }

        selectAll.setOnClickListener {
            if (tagsChipGroup.checkedChipIds.size < tagsChipGroup.childCount) {
                tagsChipGroup.check(R.id.game)
                tagsChipGroup.check(R.id.audio)
                tagsChipGroup.check(R.id.video)
                tagsChipGroup.check(R.id.image)
                tagsChipGroup.check(R.id.social)
                tagsChipGroup.check(R.id.news)
                tagsChipGroup.check(R.id.maps)
                tagsChipGroup.check(R.id.productivity)
                tagsChipGroup.check(R.id.xposed_module)
                tagsChipGroup.check(R.id.foss)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    tagsChipGroup.check(R.id.accessibility)
                }
            } else {
                tagsChipGroup.clearCheck()
            }
        }
    }

    fun setAutoTagCallback(autoTagCallback: AutoTagCallback) {
        this.autoTagCallback = autoTagCallback
    }

    companion object {
        fun newInstance(): AutoTag {
            val args = Bundle()
            val fragment = AutoTag()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showAutoTag(): AutoTag {
            val dialog = newInstance()
            dialog.show(this, TAG)
            return dialog
        }

        interface AutoTagCallback {
            fun onAutoTag(tags: Long)
        }

        const val TAG = "AutoTag"

        const val GAME = 1L shl 2
        const val AUDIO = 1L shl 3
        const val VIDEO = 1L shl 4
        const val IMAGE = 1L shl 5
        const val SOCIAL = 1L shl 6
        const val NEWS = 1L shl 7
        const val MAPS = 1L shl 8
        const val PRODUCTIVITY = 1L shl 9
        const val ACCESSIBILITY = 1L shl 10
        const val XPOSED_MODULE = 1L shl 11
        const val FOSS = 1L shl 12
    }
}
