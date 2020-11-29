package com.gigforce.app.modules.client_activation

import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.layout_confirm_driving_slot.*
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.*

class ScheduleDrivingTestFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_fragment_schedule_driving_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        txt_otp.setNumChars(4)
        resend_otp.paintFlags = resend_otp.paintFlags or Paint.UNDERLINE_TEXT_FLAG;
    }

}