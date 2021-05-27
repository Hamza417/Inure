package app.simple.inure.dialogs.miscellaneous

import android.annotation.SuppressLint
import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextInputEditText
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedDialogFragment

class IntentAction : ScopedDialogFragment() {

    private lateinit var command: TypeFaceTextView
    private lateinit var action: TypeFaceTextInputEditText
    private lateinit var launch: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_intent_action, container, false)

        command = view.findViewById(R.id.intent_command)
        action = view.findViewById(R.id.intent_action_edit_text)
        launch = view.findViewById(R.id.launch_intent_action)

        applicationInfo = requireArguments().getParcelable("application_info")!!

        return view
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        command.text = "am start -n ${applicationInfo.packageName}/${requireArguments().getString("packageId")} " +
                "-a android.intent.action.MAIN"

        action.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                command.text = "am start -n ${applicationInfo.packageName}/" +
                        "${requireArguments().getString("packageId")!!} " +
                        "-a \"${s.toString()}\""
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        launch.setOnClickListener {
            ShellExecutorDialog.newInstance(command.text.toString())
                    .show(childFragmentManager, "shell_executor")
        }
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo, packageId: String): IntentAction {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            args.putString("packageId", packageId)
            val fragment = IntentAction()
            fragment.arguments = args
            return fragment
        }
    }
}