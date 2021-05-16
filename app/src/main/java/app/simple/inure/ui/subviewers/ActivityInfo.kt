package app.simple.inure.ui.subviewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleImageButton
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.getActivities
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ActivityInfo : ScopedFragment() {

    private lateinit var name: TypeFaceTextView
    private lateinit var intentActions: TypeFaceTextView
    private lateinit var intentCategories: TypeFaceTextView
    private lateinit var backButton: DynamicRippleImageButton

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_activity_details, container, false)

        name = view.findViewById(R.id.activity_name)
        intentActions = view.findViewById(R.id.activity_info_actions)
        intentCategories = view.findViewById(R.id.activity_info_categories)
        backButton = view.findViewById(R.id.activity_info_back_button)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        startPostponedEnterTransition()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        name.text = requireArguments().getString("package_id")!!

        launch {
            var actions = ""
            var categories = ""

            withContext(Dispatchers.Default) {
                val list = applicationInfo.getActivities()!!

                for (activities in list) {
                    if (activities.name == requireArguments().getString("package_id")) {
                        for (i in activities.intentFilters) {
                            for (j in i.actions.indices) {
                                if (!actions.contains(i.actions[j])) {
                                    actions = if (actions.isEmpty()) {
                                        StringBuilder().append(i.actions[j]).toString()
                                    } else {
                                        StringBuilder().append(actions).append("\n").append(i.actions[j]).toString()
                                    }
                                }
                            }

                            for (j in i.categories.indices) {
                                if (!categories.contains(i.categories[j])) {
                                    categories = if (categories.isEmpty()) {
                                        StringBuilder().append(i.categories[j]).toString()
                                    } else {
                                        StringBuilder().append(categories).append("\n").append(i.categories[j]).toString()
                                    }
                                }
                            }
                        }
                    }
                }
            }

            this@ActivityInfo.intentActions.text = if (actions.isEmpty()) getString(R.string.not_available) else actions
            this@ActivityInfo.intentCategories.text = if (categories.isEmpty()) getString(R.string.not_available) else categories
        }

        backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    companion object {
        fun newInstance(packageId: String, applicationInfo: ApplicationInfo): ActivityInfo {
            val args = Bundle()
            args.putString("package_id", packageId)
            args.putParcelable("application_info", applicationInfo)
            val fragment = ActivityInfo()
            fragment.arguments = args
            return fragment
        }
    }
}