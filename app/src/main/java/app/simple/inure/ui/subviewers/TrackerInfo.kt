package app.simple.inure.ui.subviewers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import app.simple.inure.R
import app.simple.inure.adapters.details.AdapterTrackerDetails
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.AppIconImageView
import app.simple.inure.extensions.fragments.ScopedFragment
import app.simple.inure.glide.util.ImageLoader.loadIconFromActivityInfo
import app.simple.inure.glide.util.ImageLoader.loadIconFromServiceInfo
import app.simple.inure.interfaces.parsers.LinkCallbacks
import app.simple.inure.models.Tracker
import app.simple.inure.util.DateUtils.toDate
import app.simple.inure.util.DateUtils.toLong
import app.simple.inure.util.IntentHelper.openInBrowser
import app.simple.inure.util.ParcelUtils.parcelable
import app.simple.inure.util.StringUtils.emptyToString
import app.simple.inure.util.TextViewUtils.makeLinksClickable
import io.noties.markwon.Markwon

class TrackerInfo : ScopedFragment() {

    private lateinit var icon: AppIconImageView
    private lateinit var name: TypeFaceTextView
    private lateinit var packageId: TypeFaceTextView
    private lateinit var chips: RecyclerView
    private lateinit var trackerName: TypeFaceTextView
    private lateinit var date: TypeFaceTextView
    private lateinit var description: TypeFaceTextView
    private lateinit var codeSignature: TypeFaceTextView
    private lateinit var networkSignature: TypeFaceTextView
    private lateinit var website: TypeFaceTextView

    private var tracker: Tracker? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_tracker_info, container, false)

        icon = view.findViewById(R.id.icon)
        name = view.findViewById(R.id.name)
        packageId = view.findViewById(R.id.package_id)
        chips = view.findViewById(R.id.chips)
        trackerName = view.findViewById(R.id.tracker_name)
        date = view.findViewById(R.id.date)
        description = view.findViewById(R.id.description)
        codeSignature = view.findViewById(R.id.code_signature)
        networkSignature = view.findViewById(R.id.network_signature)
        website = view.findViewById(R.id.website)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startPostponedEnterTransition()
        tracker = requireArguments().parcelable<Tracker>(BundleConstants.trackerInfo)

        when {
            tracker?.isActivity == true -> {
                icon.loadIconFromActivityInfo(tracker?.activityInfo!!)
            }
            tracker?.isService == true -> {
                icon.loadIconFromServiceInfo(tracker?.serviceInfo!!)
            }
            tracker?.isReceiver == true -> {
                icon.loadIconFromActivityInfo(tracker?.receiverInfo!!)
            }
        }

        chips.apply {
            val data = arrayListOf<String>()

            when {
                tracker?.isActivity == true -> {
                    data.add(getString(R.string.activity))
                }
                tracker?.isService == true -> {
                    data.add(getString(R.string.service))
                }
                tracker?.isReceiver == true -> {
                    data.add(getString(R.string.receiver))
                }
            }

            tracker?.categories?.forEach {
                data.add(it)
            }

            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = AdapterTrackerDetails(data)
        }

        name.text = tracker?.componentName?.substringAfterLast(".")
        packageId.text = tracker?.componentName
        trackerName.text = tracker?.name
        date.apply {
            val text = getString(R.string.created_on, tracker?.creationDate
                ?.toLong() // Convert to long, probably a timestamp in format yyyy-MM-dd
                ?.toDate(DATE_FORMAT)) // Convert to fancy date format using the pattern "EEE, yyyy MMM dd"
                .prependIndent(MARKDOWN_BULLET_PREFIX) // Add a bullet point to the start of the string

            Markwon.create(requireContext()).setMarkdown(this, text)
        }

        description.apply {
            val markdown = Markwon.create(requireContext())
            val spanned = markdown.toMarkdown(tracker?.description ?: "")

            makeLinksClickable(spanned, LinkCallbacks { url, _ ->
                url.toUri().openInBrowser(requireContext())
            })
        }

        codeSignature.apply {
            val text = tracker?.codeSignature?.emptyToString(getString(R.string.not_available))
                ?.replace("|", MARKDOWN_LINE_BREAK)
                ?.prependIndent(MARKDOWN_BULLET_PREFIX)!!

            Markwon.create(requireContext()).setMarkdown(this, text)
        }

        networkSignature.apply {
            val text = tracker?.networkSignature?.emptyToString(getString(R.string.not_available))
                ?.replace("|", MARKDOWN_LINE_BREAK)
                ?.replace("\\\\", "") // Remove any backslashes for a clean URL
                ?.prependIndent(MARKDOWN_BULLET_PREFIX)!!

            Markwon.create(requireContext()).setMarkdown(this, text)
        }

        website.apply {
            val text = tracker?.website?.emptyToString(getString(R.string.not_available))
                ?.replace("|", MARKDOWN_LINE_BREAK)
                ?.replace("\\\\", "") // Remove any backslashes for a clean URL
                ?.prependIndent(MARKDOWN_BULLET_PREFIX)!!

            val markdown = Markwon.create(requireContext())
            val spanned = markdown.toMarkdown(text)

            makeLinksClickable(spanned, LinkCallbacks { url, _ ->
                url.toUri().openInBrowser(requireContext())
            })
        }
    }

    companion object {
        fun newInstance(tracker: Tracker): TrackerInfo {
            val args = Bundle()
            args.putParcelable(BundleConstants.trackerInfo, tracker)
            val fragment = TrackerInfo()
            fragment.arguments = args
            return fragment
        }

        const val TAG = "TrackerInfo"
        private const val DATE_FORMAT = "EEE, yyyy MMM dd"
        private const val MARKDOWN_BULLET_PREFIX = "* "
        private const val MARKDOWN_LINE_BREAK = "\r\n"
    }
}