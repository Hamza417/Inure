package app.simple.inure.ui.viewers

import android.content.pm.ApplicationInfo
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import app.simple.inure.R
import app.simple.inure.decorations.views.TypeFaceTextView
import app.simple.inure.extension.fragments.ScopedFragment
import app.simple.inure.util.APKParser.getCertificates
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Certificate : ScopedFragment() {

    private lateinit var algorithm: TypeFaceTextView
    private lateinit var oid: TypeFaceTextView
    private lateinit var base64md5: TypeFaceTextView
    private lateinit var md5: TypeFaceTextView
    private lateinit var validity: TypeFaceTextView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_certificate, container, false)

        algorithm = view.findViewById(R.id.certificate_algorithm)
        oid = view.findViewById(R.id.certificate_algorithm_oid)
        base64md5 = view.findViewById(R.id.certificate_base64_md5)
        md5 = view.findViewById(R.id.certificate_md5)
        validity = view.findViewById(R.id.certificate_validity)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        startPostponedEnterTransition()

        launch {
            val algorithm: String
            val oid: String
            val base64md5: String
            val md5: String
            val validity: String

            withContext(Dispatchers.Default) {
                val cert = requireArguments().getParcelable<ApplicationInfo>("application_info")?.getCertificates()!!

                algorithm = cert.signAlgorithm
                oid = cert.signAlgorithmOID
                base64md5 = cert.certBase64Md5
                md5 = cert.certMd5
                validity = "${cert.startDate} - ${cert.endDate}"
            }

            this@Certificate.algorithm.text = algorithm
            this@Certificate.oid.text = oid
            this@Certificate.base64md5.text = base64md5
            this@Certificate.md5.text = md5
            this@Certificate.validity.text = validity
        }
    }

    companion object {
        fun newInstance(applicationInfo: ApplicationInfo): Certificate {
            val args = Bundle()
            args.putParcelable("application_info", applicationInfo)
            val fragment = Certificate()
            fragment.arguments = args
            return fragment
        }
    }
}