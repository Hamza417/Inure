package app.simple.inure.dialogs.trackers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.util.ParcelUtils.serializable

class TrackersMessage : ScopedBottomSheetFragment() {

    private lateinit var title: TypeFaceTextView
    private lateinit var message: TypeFaceTextView

    private var data: Pair<String, String>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_trackers_message, container, false)

        title = view.findViewById(R.id.title)
        message = view.findViewById(R.id.trackers_message)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        data = requireArguments().serializable(BundleConstants.trackersMessage) as Pair<String, String>?

        title.text = data!!.first
        message.text = data!!.second
    }

    companion object {
        fun newInstance(message: Pair<String, String>?): TrackersMessage {
            val args = Bundle()
            args.putSerializable(BundleConstants.trackersMessage, message)
            val fragment = TrackersMessage()
            fragment.arguments = args
            return fragment
        }
    }
}