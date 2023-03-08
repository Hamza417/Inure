package app.simple.inure.extensions.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.LoaderImageView
import app.simple.inure.preferences.ConfigurationPreferences

open class ScopedActionDialogBottomFragment : ScopedBottomSheetFragment() {

    protected lateinit var mode: TypeFaceTextView
    protected lateinit var packageName: TypeFaceTextView
    protected lateinit var loader: LoaderImageView
    protected lateinit var status: TypeFaceTextView

    var onSuccess: (() -> Unit)? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val view = inflater.inflate(getLayoutViewId(), container, false)

        mode = view.findViewById(R.id.mode)
        packageName = view.findViewById(R.id.package_name)
        loader = view.findViewById(R.id.loader)
        status = view.findViewById(R.id.result)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (ConfigurationPreferences.isUsingRoot()) {
            mode.setText(R.string.root)
        } else if (ConfigurationPreferences.isUsingShizuku()) {
            mode.setText(R.string.shizuku)
        }

        packageName.text = packageInfo.packageName
    }

    open fun getLayoutViewId(): Int {
        return R.layout.dialog_enable_disable
    }
}