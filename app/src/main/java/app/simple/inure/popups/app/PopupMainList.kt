package app.simple.inure.popups.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import android.widget.TextView
import app.simple.inure.R
import app.simple.inure.apk.utils.PackageUtils
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extension.popup.BasePopupWindow
import app.simple.inure.extension.popup.PopupLinearLayout
import app.simple.inure.extension.popup.PopupMenuCallback
import app.simple.inure.util.ViewUtils.gone
import app.simple.inure.util.ViewUtils.visible

/**
 * A customised version of popup menu that uses [PopupWindow]
 * created to replace ugly material popup menu which does not
 * provide any customizable flexibility. This on the other hand
 * uses custom layout, background, animations and also dims entire
 * window when appears. It is highly recommended to use this
 * and ditch popup menu entirely.
 */
class PopupMainList(anchor: View, packageName: String) : BasePopupWindow() {

    lateinit var popupMenuCallback: PopupMenuCallback
    private val launch: DynamicRippleTextView
    private val copyPackageName: DynamicRippleTextView

    init {
        val contentView = LayoutInflater.from(anchor.context).inflate(R.layout.popup_main_list_menu, PopupLinearLayout(anchor.context))

        contentView.findViewById<DynamicRippleTextView>(R.id.popup_app_info).onClick()
        contentView.findViewById<DynamicRippleTextView>(R.id.popup_send).onClick()

        launch = contentView.findViewById(R.id.popup_app_launch)
        copyPackageName = contentView.findViewById(R.id.popup_copy_package)

        if (PackageUtils.checkIfAppIsLaunchable(contentView.context, packageName) && packageName != anchor.context.packageName) {
            launch.visible(false)
            launch.onClick()
        } else {
            launch.gone()
        }

        copyPackageName.setOnClickListener {
            val clipBoard = contentView.context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("Package Name", packageName)
            clipBoard.setPrimaryClip(clipData)
            dismiss()
        }

        init(contentView, anchor, Gravity.END)
    }

    private fun TextView.onClick() {
        this.setOnClickListener {
            popupMenuCallback.onMenuItemClicked(this.text.toString())
            dismiss()
        }
    }

    fun setOnMenuItemClickListener(popupMenuCallback: PopupMenuCallback) {
        this.popupMenuCallback = popupMenuCallback
    }
}
