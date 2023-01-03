package app.simple.inure.dialogs.trackers

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.interfaces.fragments.WarningCallbacks
import app.simple.inure.models.TrackersMessageModel
import app.simple.inure.util.ParcelUtils.parcelable

class TrackersMessage : ScopedBottomSheetFragment() {

    private lateinit var title: TypeFaceTextView
    private lateinit var message: TypeFaceTextView

    private var trackersMessageModel: TrackersMessageModel? = null
    private var warningCallbacks: WarningCallbacks? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_trackers_message, container, false)

        title = view.findViewById(R.id.title)
        message = view.findViewById(R.id.trackers_message)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trackersMessageModel = requireArguments().parcelable(BundleConstants.trackersMessage) as TrackersMessageModel?

        title.text = trackersMessageModel!!.title
        message.text = trackersMessageModel!!.message
    }

    fun setWarningCallbacks(warningCallbacks: WarningCallbacks) {
        this.warningCallbacks = warningCallbacks
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        warningCallbacks?.onWarningDismissed()
    }

    companion object {
        fun newInstance(message: TrackersMessageModel?): TrackersMessage {
            val args = Bundle()
            args.putParcelable(BundleConstants.trackersMessage, message)
            val fragment = TrackersMessage()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showTrackersMessage(message: TrackersMessageModel?): TrackersMessage {
            val fragment = newInstance(message)
            fragment.show(this, "trackers_message")
            return fragment
        }
    }
}