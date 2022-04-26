package com.gigforce.client_activation.client_activation

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.client_activation.R
import com.gigforce.client_activation.client_activation.models.PartnerSchoolDetails
import com.gigforce.common_ui.StringConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ConfirmationDialogDrivingTest //: BottomSheetDialogFragment(), TimeSlotsDialog.TimeSlotDialogCallbacks
{
//    private var dateSelected: String? = null
//    private var timeSlot: String? = null
//    private var selectedPartner: PartnerSchoolDetails? = null
//    private lateinit var mJobProfileId: String
//    private lateinit var mTitle: String
//    private lateinit var mType: String
//    private lateinit var callbacks: ConfirmationDialogDrivingTestCallbacks
//    private val viewModel: ConfirmationDialogDrivingTestViewModel by viewModels()
//
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.layout_confirm_driving_slot, container, false)
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        getDataFromIntents(savedInstanceState)
//        initView()
//        initObservers()
//    }
//
//    private fun getDataFromIntents(savedInstanceState: Bundle?) {
//        savedInstanceState?.let {
//            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
//            mType = it.getString(StringConstants.TYPE.value) ?: return@let
//            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
//        }
//
//        arguments?.let {
//            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
//            mType = it.getString(StringConstants.TYPE.value) ?: return@let
//            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
//        }
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)
//        outState.putString(StringConstants.TYPE.value, mType)
//        outState.putString(StringConstants.TITLE.value, mTitle)
//
//
//    }
//
//    private fun initObservers() {
//        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer {
//
//
//            if (it) {
//                pb_conf_dialog.gone()
//                callbacks.submissionSuccess()
//                this@ConfirmationDialogDrivingTest.dismiss()
//
//            }
//        })
//
//    }
//
//    private fun initView() {
//        tv_change_slot.paintFlags = tv_change_slot.paintFlags or Paint.UNDERLINE_TEXT_FLAG
//        selectedPartner =
//            arguments?.getParcelable<PartnerSchoolDetails>(StringConstants.SELECTED_PARTNER.value)
//        timeSlot = arguments?.getString(StringConstants.SELECTED_TIME_SLOT.value)
//        dateSelected = arguments?.getString(StringConstants.SELECTED_DATE.value)
//        textView137.text =
//            Html.fromHtml((if (selectedPartner?.name == null) "" else selectedPartner?.name + "<br>") +
//                    (if (selectedPartner?.line1 == null) "" else selectedPartner?.line1 + "<br>") +
//                    (if (selectedPartner?.line2 == null) "" else selectedPartner?.line2 + "<br>") +
//                    (if (selectedPartner?.line3 == null) "" else selectedPartner?.line3 + "<br>") +
//                    if (selectedPartner?.contact.isNullOrEmpty()) "" else
//                        selectedPartner?.contact?.map { "<b><font color=\'#000000\'>" + it.name + "</font></b><br>" }
//                            ?.reduce { a, o -> a + o }
//            )
//        textView143.text = timeSlot
//        textView139.text = dateSelected
//
//        if (!selectedPartner?.lat.isNullOrEmpty()) {
//            iv_location.setOnClickListener {
//                val uri =
//                    "http://maps.google.com/maps?saddr=" + "&daddr=" + selectedPartner?.lat + "," + selectedPartner?.lon
//                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
//                startActivity(intent)
//            }
//        } else {
//            iv_location.gone()
//        }
//
//        iv_contact.setOnClickListener {
//            if (!selectedPartner?.contact.isNullOrEmpty()) {
//                val callIntent = Intent(Intent.ACTION_DIAL)
//                callIntent.data = Uri.parse("tel: " + selectedPartner?.contact!![0].number)
//                startActivity(callIntent)
//            }
//        }
//
//
//        tv_change_slot.setOnClickListener {
//            callbacks.changeSlot()
//            dismiss()
//        }
//
//        slider_confirm.onSlideCompleteListener =
//            object : SlideToActView.OnSlideCompleteListener {
//
//                override fun onSlideComplete(view: SlideToActView) {
//                    pb_conf_dialog.visible()
//                    viewModel.apply(
//                        mJobProfileId,
//                        selectedPartner!!,
//                        dateSelected!!,
//                        timeSlot!!,
//                        cb_centre.isChecked, mType, mTitle
//                    )
//                }
//            }
//
//        cb_centre.setOnClickListener {
//            slider_confirm.isLocked = !cb_centre.isChecked
//        }
//
//
//    }
//
//    override fun setSelectedTimeSlot(time: String) {
//        textView143.text = time
//    }
//
//    fun setCallbacks(callbacks: ConfirmationDialogDrivingTestCallbacks) {
//        this.callbacks = callbacks
//    }
//
//    interface ConfirmationDialogDrivingTestCallbacks {
//        fun moveToNextStep()
//        fun submissionSuccess()
//        fun changeSlot()
//    }
}