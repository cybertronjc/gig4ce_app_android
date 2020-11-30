package com.gigforce.app.modules.client_activation

import android.graphics.Paint
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
import kotlinx.android.synthetic.main.layout_fragment_schedule_driving_test.*

class ConfirmationDialogDrivingTest : BottomSheetDialogFragment(), TimeSlotsDialog.TimeSlotDialogCallbacks {
    private lateinit var mWordOrderID: String
    private lateinit var callbacks: ConfirmationDialogDrivingTestCallbacks
    private val viewModel: ConfirmationDialogDrivingTestViewModel by viewModels()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
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

        }

        arguments?.let {
            mWordOrderID = it.getString(StringConstants.WORK_ORDER_ID.value) ?: return@let

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.WORK_ORDER_ID.value, mWordOrderID)


    }

    private fun initObservers() {
        viewModel.observableJpApplication.observe(viewLifecycleOwner, Observer {


            if (it) {
                pb_conf_dialog.gone()
                this@ConfirmationDialogDrivingTest.dismiss()

            }
        })

    }

    private fun initView() {
        tv_change_slot.paintFlags = tv_change_slot.paintFlags or Paint.UNDERLINE_TEXT_FLAG;
        val selectedPartner = arguments?.getParcelable<PartnerSchoolDetails>(StringConstants.SELECTED_PARTNER.value)
        val timeSlot = arguments?.getString(StringConstants.SELECTED_TIME_SLOT.value)
        val dateSelected = arguments?.getString(StringConstants.SELECTED_DATE.value)
        textView137.text = Html.fromHtml(selectedPartner?.schoolName + "<br>" + selectedPartner?.landmark + "<br>" + selectedPartner?.city + "<br>"
                + selectedPartner?.schoolTiming + "<br>" + selectedPartner?.contact?.map { "<b><font color=\'#000000\'>" + it.name + "</font></b>" }?.reduce { a, o -> a + o }
        )
        textView143.text = timeSlot
        textView139.text = dateSelected


        tv_change_slot.setOnClickListener {
            val newInstance = TimeSlotsDialog.newInstance()
            newInstance.setCallbacks(this)
            newInstance.show(parentFragmentManager, TimeSlotsDialog::class.java.name)
        }

        slider_confirm.onSlideCompleteListener =
                object : SlideToActView.OnSlideCompleteListener {

                    override fun onSlideComplete(view: SlideToActView) {
                        pb_conf_dialog.visible()
                        viewModel.apply(mWordOrderID, selectedPartner!!, dateSelected!!, timeSlot!!, cb_centre.isChecked)
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
    }
}