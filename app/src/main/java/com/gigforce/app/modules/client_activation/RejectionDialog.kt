package com.gigforce.app.modules.client_activation

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.app.R
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_confirm_driving_slot.*
import kotlinx.android.synthetic.main.layout_rejection_dialog.*

class RejectionDialog : DialogFragment() {
    private lateinit var callbacks: RejectionDialogCallbacks

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_rejection_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(getScreenWidth(requireActivity()).width - resources.getDimensionPixelSize(R.dimen.size_48), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClicks()
        initView()
    }

    private fun initView() {
        tv_take_me_home_rejection_dialog.paintFlags = tv_take_me_home_rejection_dialog.paintFlags or Paint.UNDERLINE_TEXT_FLAG;

    }

    private fun initClicks() {
        tv_refer_rejection_dialog.setOnClickListener {
            dismiss()
            callbacks.onClickRefer()

        }
        tv_take_me_home_rejection_dialog.setOnClickListener {
            dismiss()
            callbacks.onClickTakMeHome()

        }
    }

    fun setCallbacks(callbacks: RejectionDialogCallbacks) {
        this.callbacks = callbacks
    }

    interface RejectionDialogCallbacks {
        fun onClickRefer()
        fun onClickTakMeHome()
    }


}