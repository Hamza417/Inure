package app.simple.inure.popups.viewers

import android.view.LayoutInflater
import android.view.View
import app.simple.inure.R
import app.simple.inure.constants.Misc
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.popup.BasePopupWindow
import app.simple.inure.extensions.popup.PopupLinearLayout
import app.simple.inure.extensions.popup.PopupMenuCallback
import app.simple.inure.preferences.ConfigurationPreferences
import app.simple.inure.util.ViewUtils.gone

class PopupActivitiesMenu(view: View, isComponentEnabled: Boolean) : BasePopupWindow() {

    private lateinit var popupMainMenuCallbacks: PopupMenuCallback
    private var componentState: DynamicRippleTextView
    private var forceLaunch: DynamicRippleTextView
    private var forceLaunchWithAction: DynamicRippleTextView
    private var createShortcut: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(view.context).inflate(R.layout.popup_activities_menu, PopupLinearLayout(view.context))
        val context = contentView.context

        componentState = contentView.findViewById(R.id.popup_component_state_toggle)

        componentState.text = if (isComponentEnabled) {
            contentView.context.getString(R.string.disable)
        } else {
            contentView.context.getString(R.string.enable)
        }.also {
            componentState.onClick(it)
        }

        forceLaunch = contentView.findViewById(R.id.popup_launch)
        forceLaunchWithAction = contentView.findViewById(R.id.popup_launch_with_action)
        createShortcut = contentView.findViewById(R.id.popup_create_shortcut)

        if (ConfigurationPreferences.isRootOrShizuku()) {
            forceLaunch.visibility = View.VISIBLE
            forceLaunchWithAction.visibility = View.VISIBLE
            componentState.visibility = View.VISIBLE
        } else {
            forceLaunch.visibility = View.GONE
            forceLaunchWithAction.visibility = View.GONE
            componentState.visibility = View.GONE
        }

        forceLaunch.onClick(context.getString(R.string.force_launch))
        forceLaunchWithAction.onClick(context.getString(R.string.force_launch_with_action))
        createShortcut.onClick(context.getString(R.string.create_shortcut))

        if (!isComponentEnabled) {
            forceLaunch.gone()
            forceLaunchWithAction.gone()
        }

        init(contentView, view, Misc.xOffset, Misc.yOffset)

        setOnDismissListener {
            popupMainMenuCallbacks.onDismiss()
        }
    }

    private fun DynamicRippleTextView.onClick(string: String) {
        this.setOnClickListener {
            popupMainMenuCallbacks.onMenuItemClicked(string)
            dismiss()
        }
    }

    fun setOnMenuClickListener(popupMainMenuCallbacks: PopupMenuCallback) {
        this.popupMainMenuCallbacks = popupMainMenuCallbacks
    }
}
