package com.gigforce.client_activation.client_activation

import android.os.Bundle
import android.text.Html
import com.bumptech.glide.Glide
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.dialog.RejectionDialog
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import kotlinx.android.synthetic.main.layout_rejection_dialog.*

class OTPVerifiedDialog : RejectionDialog() {
    companion object {
        const val OTP_VERIFY_SUCCESS = 4
    }

    private var SUBTITLE: String? = null
    private var ACTIONMAIN: String? = null
    private var ACTIONSEC: String? = null

    override fun getDataFromIntents(savedInstanceState: Bundle?) {
        super.getDataFromIntents(savedInstanceState)

        savedInstanceState?.let {

            SUBTITLE = it.getString(StringConstants.SUBTITLE.value) ?: ""
            ACTIONMAIN = it.getString(StringConstants.ACTION_MAIN.value) ?: ""
            ACTIONSEC = it.getString(StringConstants.ACTION_SEC.value) ?: ""

        }

        arguments?.let {
            SUBTITLE = it.getString(StringConstants.SUBTITLE.value) ?: ""
            ACTIONMAIN = it.getString(StringConstants.ACTION_MAIN.value) ?: ""
            ACTIONSEC = it.getString(StringConstants.ACTION_SEC.value) ?: ""

        }
    }

    override fun initUiAsPerState() {
        stateOtpVerified()
    }

    private fun stateOtpVerified() {
        setupRecycler()
        tv_sub_one_rejection_dialog.text = Html.fromHtml(SUBTITLE)
        tv_sub_two_rejection_dialog.gone()
        tv_content_title_rejection_dialog.text = Html.fromHtml(TITLE)
        Glide.with(this).load(ILLUSTRATION).placeholder(
            getCircularProgressDrawable(
                requireContext()
            )
        ).into(iv_rejection_illustration)
        tv_refer_rejection_dialog.text = ACTIONMAIN
        tv_take_me_home_rejection_dialog.text = ACTIONSEC
        rv_wrong_questions_rejection_dialog.visible()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.SUBTITLE.value, SUBTITLE)
        outState.putString(StringConstants.ACTION_MAIN.value, ACTIONMAIN)
        outState.putString(StringConstants.ACTION_SEC.value, ACTIONSEC)
    }

}