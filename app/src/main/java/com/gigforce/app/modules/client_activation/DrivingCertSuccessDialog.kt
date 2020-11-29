package com.gigforce.app.modules.client_activation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.app.utils.getScreenWidth

class DrivingCertSuccessDialog : DialogFragment() {

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
}