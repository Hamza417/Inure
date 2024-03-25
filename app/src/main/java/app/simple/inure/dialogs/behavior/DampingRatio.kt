package app.simple.inure.dialogs.behavior

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.FragmentManager
import app.simple.inure.R
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.decorations.theme.ThemeSeekBar
import app.simple.inure.decorations.typeface.TypeFaceTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment
import app.simple.inure.preferences.BehaviourPreferences

class DampingRatio : ScopedBottomSheetFragment() {

    private lateinit var value: TypeFaceTextView
    private lateinit var seekbar: ThemeSeekBar
    private lateinit var set: DynamicRippleTextView
    private lateinit var cancel: DynamicRippleTextView

    private var oldDampingRatio = BehaviourPreferences.getDampingRatio()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.dialog_stiffness, container, false)

        value = view.findViewById(R.id.value)
        seekbar = view.findViewById(R.id.seekbar)
        set = view.findViewById(R.id.set)
        cancel = view.findViewById(R.id.cancel)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        seekbar.max = MAX_DAMPING_RATIO
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            seekbar.min = MIN_DAMPING_RATIO
        }
        seekbar.progress = (BehaviourPreferences.getDampingRatio() * MAX_DAMPING_RATIO).toInt()
        value.text = (BehaviourPreferences.getDampingRatio() * MAX_DAMPING_RATIO).toInt().toString()

        seekbar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    value.text = progress.toString()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                /* no-op */
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                /* no-op */
            }
        })

        set.setOnClickListener {
            BehaviourPreferences.setDampingRatio(seekbar.progress / MAX_DAMPING_RATIO.toFloat())
            dismiss()
        }

        cancel.setOnClickListener {
            BehaviourPreferences.setDampingRatio(oldDampingRatio)
            dismiss()
        }
    }

    companion object {
        fun newInstance(): DampingRatio {
            val args = Bundle()
            val fragment = DampingRatio()
            fragment.arguments = args
            return fragment
        }

        fun FragmentManager.showDampingRatioDialog() {
            val dialog = newInstance()
            dialog.show(this, TAG)
        }

        private const val MAX_DAMPING_RATIO = 100
        private const val MIN_DAMPING_RATIO = 10

        const val TAG = "DampingRatio"
    }
}
