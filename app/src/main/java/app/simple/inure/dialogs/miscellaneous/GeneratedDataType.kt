package app.simple.inure.dialogs.miscellaneous

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import app.simple.inure.decorations.ripple.DynamicRippleTextView
import app.simple.inure.extensions.fragments.ScopedBottomSheetFragment

class GeneratedDataType : ScopedBottomSheetFragment() {

    private lateinit var txt: DynamicRippleTextView
    private lateinit var xml: DynamicRippleTextView

    private var onDataTypeSelected: OnDataTypeSelected? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(app.simple.inure.R.layout.dialog_generated_data_type, container, false)

        txt = view.findViewById(app.simple.inure.R.id.txt)
        xml = view.findViewById(app.simple.inure.R.id.xml)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        txt.setOnClickListener {
            onDataTypeSelected?.onDataTypeSelected(TYPE_TXT)
            dismiss()
        }

        xml.setOnClickListener {
            onDataTypeSelected?.onDataTypeSelected(TYPE_XML)
            dismiss()
        }
    }

    fun setOnDataTypeSelected(onDataTypeSelected: OnDataTypeSelected) {
        this.onDataTypeSelected = onDataTypeSelected
    }

    companion object {
        fun newInstance(): GeneratedDataType {
            return GeneratedDataType()
        }

        fun FragmentManager.showGeneratedDataTypeSelector(): GeneratedDataType {
            val fragment = newInstance()
            fragment.show(this, "generated_data_type")
            return fragment
        }

        interface OnDataTypeSelected {
            fun onDataTypeSelected(type: String)
        }

        const val TYPE_TXT = "txt"
        const val TYPE_XML = "xml"
    }
}