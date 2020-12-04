package com.gigforce.app.modules.client_activation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_dialog_driving_certificate_success.*

class DrivingCertSuccessDialog : DialogFragment() {

    private lateinit var callbacks: DrivingCertSuccessDialogCallbacks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_dialog_driving_certificate_success, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(getScreenWidth(requireActivity()).width - resources.getDimensionPixelSize(R.dimen.size_32), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
    }

    private fun initClicks() {
        tv_okay_driving_cert_success.setOnClickListener {
            dismiss()
            callbacks.onClickOkay()
        }
    }

    fun setCallbacks(callbacks: DrivingCertSuccessDialogCallbacks) {
        this.callbacks = callbacks
    }

    interface DrivingCertSuccessDialogCallbacks {
        fun onClickOkay()
    }
}