package app.simple.inure.dialogs.miscellaneous

import android.annotation.SuppressLint
import android.content.pm.PackageInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import app.simple.inure.R
import app.simple.inure.constants.BundleConstants
import app.simple.inure.decorations.corners.DynamicCornerEditText
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.decorations.views.CustomProgressBar
import app.simple.inure.extensions.fragments.ScopedDialogFragment
import app.simple.inure.factories.actions.ActivityLaunchFactory
import app.simple.inure.util.ConditionUtils.isZero
import app.simple.inure.util.ViewUtils.invisible
import app.simple.inure.util.ViewUtils.visible
import app.simple.inure.viewmodels.dialogs.ActivityLauncherViewModel

class IntentAction : ScopedDialogFragment() {

    private lateinit var command: TypeFaceTextView
    private lateinit var action: DynamicCornerEditText
    private lateinit var launch: TypeFaceTextView
    private lateinit var loader: CustomProgressBar

    private lateinit var activityLaunchFactory: ActivityLaunchFactory
    private lateinit var activityLauncherViewModel: ActivityLauncherViewModel

    private var packageId: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_intent_action, container, false)

        command = view.findViewById(R.id.intent_command)
        action = view.findViewById(R.id.intent_action_edit_text)
        launch = view.findViewById(R.id.launch_intent_action)
        loader = view.findViewById(R.id.loader)

        packageId = requireArguments().getString(BundleConstants.packageId)!!

        activityLaunchFactory = ActivityLaunchFactory(packageInfo, packageId!!)
        activityLauncherViewModel = ViewModelProvider(this, activityLaunchFactory)[ActivityLauncherViewModel::class.java]

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        command.text = "am start -n ${packageInfo.packageName}/${packageId} " +
                "-a \"android.intent.action.MAIN\""

        action.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (count.isZero()) {
                    command.text = "am start -n ${packageInfo.packageName}/${packageId} " +
                            "-a \"android.intent.action.MAIN\""
                } else {
                    command.text = "am start -n ${packageInfo.packageName}/" +
                            "$packageId -a \"${s.toString()}\""
                }
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        launch.setOnClickListener {
            loader.visible(true)
            activityLauncherViewModel.runActionCommand(action.text.toString())
        }

        activityLauncherViewModel.getActionStatus().observe(viewLifecycleOwner) {
            when (it) {
                "Done", "Failed" -> {
                    loader.invisible(true)
                }
            }
        }
    }

    companion object {
        fun newInstance(packageInfo: PackageInfo, packageId: String): IntentAction {
            val args = Bundle()
            args.putParcelable(BundleConstants.packageInfo, packageInfo)
            args.putString(BundleConstants.packageId, packageId)
            val fragment = IntentAction()
            fragment.arguments = args
            return fragment
        }
    }
}