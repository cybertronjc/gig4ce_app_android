package com.gigforce.app.modules.client_activation

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
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.client_activation.models.PartnerSchoolDetails
import com.gigforce.app.utils.StringConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ncorti.slidetoact.SlideToActView
import kotlinx.android.synthetic.main.layout_confirm_driving_slot.*

class ConfirmationDialogDrivingTest : BottomSheetDialogFragment(),
        TimeSlotsDialog.TimeSlotDialogCallbacks {
    private lateinit var mWordOrderID: String
    private lateinit var mTitle: String
    private lateinit var mType: String
    private lateinit var callbacks: ConfirmationDialogDrivingTestCallbacks
    private val viewModel: ConfirmationDialogDrivingTestViewModel by viewModels()


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_confirm_driving_slot, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initView()
        initObservers()
    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)
        outState.putString(StringConstants.TYPE.value, mType)
        outState.putString(StringConstants.TITLE.value, mTitle)


    }

    private fun initObservers() {
        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer {


            if (it) {
                pb_conf_dialog.gone()
                callbacks.submissionSuccess()
                this@ConfirmationDialogDrivingTest.dismiss()

            }
        })

    }

    private fun initView() {
        tv_change_slot.paintFlags = tv_change_slot.paintFlags or Paint.UNDERLINE_TEXT_FLAG;
        val selectedPartner =
                arguments?.getParcelable<PartnerSchoolDetails>(StringConstants.SELECTED_PARTNER.value)
        val timeSlot = arguments?.getString(StringConstants.SELECTED_TIME_SLOT.value)
        val dateSelected = arguments?.getString(StringConstants.SELECTED_DATE.value)
        textView137.text =
                Html.fromHtml(selectedPartner?.name + "<br>" + selectedPartner?.landmark + "<br>" + selectedPartner?.city + "<br>"
                        + selectedPartner?.name + "<br>" + selectedPartner?.contact?.map { "<b><font color=\'#000000\'>" + it.name + "</font></b>" }
                        ?.reduce { a, o -> a + o }
                )
        textView143.text = timeSlot
        textView139.text = dateSelected
        iv_location.setOnClickListener {
            val uri =
                    "http://maps.google.com/maps?saddr=" + "&daddr=" + selectedPartner?.lat + "," + selectedPartner?.lon
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            startActivity(intent)
        }
        iv_contact.setOnClickListener {
            if (!selectedPartner?.contact.isNullOrEmpty()) {
                val callIntent = Intent(Intent.ACTION_DIAL);
                callIntent.data = Uri.parse("tel: " + selectedPartner?.contact!![0].number);
                startActivity(callIntent);
            }
        }


        tv_change_slot.setOnClickListener {
            val newInstance = TimeSlotsDialog.newInstance()
            newInstance.setCallbacks(this)
            newInstance.show(parentFragmentManager, TimeSlotsDialog::class.java.name)
        }

        slider_confirm.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {

                    override fun onSlideComplete(view: SlideToActView) {
                        pb_conf_dialog.visible()
                        viewModel.apply(
                                mWordOrderID,
                                selectedPartner!!,
                                dateSelected!!,
                                timeSlot!!,
                                cb_centre.isChecked, mType, mTitle
                        )
                    }
                }


    }

    override fun setSelectedTimeSlot(time: String) {
        textView143.text = time
    }

    fun setCallbacks(callbacks: ConfirmationDialogDrivingTestCallbacks) {
        this.callbacks = callbacks
    }

    public interface ConfirmationDialogDrivingTestCallbacks {
        fun moveToNextStep()
        fun submissionSuccess()
    }
}