package com.gigforce.app.modules.client_activation

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.modules.client_activation.models.JpSettings
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_dialog_submit_application.*

class ReviewApplicationDialogClientActivation : DialogFragment() {
    private lateinit var callbacks: ReviewApplicationDialogCallbacks
    private lateinit var jpSettings: JpSettings

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_dialog_submit_application, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {

            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog.window?.setLayout(
                getScreenWidth(
                    requireActivity()
                ).width - resources.getDimensionPixelSize(R.dimen.size_48), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initClicks()
        initView()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putParcelable(StringConstants.JOB_PROFILE_ID.value, jpSettings)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            jpSettings = it.getParcelable<JpSettings>(StringConstants.DATA.value) ?: return@let

        }

        arguments?.let {
            jpSettings = it.getParcelable<JpSettings>(StringConstants.DATA.value) ?: return@let

        }
    }


    private fun initView() {

//        tv_take_me_home_rejection_dialog.paintFlags = tv_take_me_home_rejection_dialog.paintFlags or Paint.UNDERLINE_TEXT_FLAG;
        tv_title_submit_application.text = jpSettings.completionTitle
        tv_content_submit_application.text = Html.fromHtml(jpSettings.completionMessage)
        Glide.with(this).load(jpSettings?.completionImage).placeholder(
            getCircularProgressDrawable(
                requireContext()
            )
        ).into(iv_submit_application)


    }

    private fun initClicks() {
        tv_submit_application.setOnClickListener {
            callbacks.onClickSubmit()

        }
        tv_review_submit_application.setOnClickListener {
            callbacks.onClickReview()


        }
    }

    fun setCallbacks(callbacks: ReviewApplicationDialogCallbacks) {
        this.callbacks = callbacks
    }

    interface ReviewApplicationDialogCallbacks {
        fun onClickSubmit()

        fun onClickReview()


    }

}