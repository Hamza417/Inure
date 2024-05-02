package app.simple.inure.dialogs.tags

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class TagsMenu : ScopedBottomSheetFragment() {

    private lateinit var autoTag: DynamicRippleTextView
    private lateinit var openSettings: DynamicRippleTextView
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_menu_tags, container, false)

        autoTag = view.findViewById(R.id.auto_tag)
        openSettings = view.findViewById(R.id.open_settings)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        autoTag.setOnClickListener {

        }

        openSettings.setOnClickListener {
            openSettings()
        }
    }

    companion object {
        fun newInstance(): TagsMenu {
            val args = Bundle()
            val fragment = TagsMenu()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showTagsMenu() {
            newInstance().show(this, TAG)
        }

        const val TAG = "TagsMenu"
    }
}
