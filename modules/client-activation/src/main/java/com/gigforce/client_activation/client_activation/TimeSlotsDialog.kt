package com.gigforce.client_activation.client_activation

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.utils.getScreenWidth
import java.util.*


class TimeSlotsDialog //: DialogFragment()
{
//    private lateinit var callbacks: TimeSlotDialogCallbacks
//    private lateinit var timeSlots: ArrayList<String>
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.fragment_slots_driving_test, container, false)
//    }
//
//    override fun onStart() {
//        super.onStart()
//        val dialog: Dialog? = dialog
//        if (dialog != null) {
//            dialog.window?.setLayout(
//                getScreenWidth(requireActivity()).width - resources.getDimensionPixelSize(
//                    R.dimen.size_32
//                ), ViewGroup.LayoutParams.WRAP_CONTENT
//            )
//        }
//    }
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        outState.putStringArrayList(StringConstants.TIME_SLOTS.value, timeSlots)
//
//
//    }
//
//
//    private fun getDataFromIntents(savedInstanceState: Bundle?) {
//        savedInstanceState?.let {
//            timeSlots = it.getStringArrayList(StringConstants.TIME_SLOTS.value)!!
//        }
//
//        arguments?.let {
//            timeSlots = it.getStringArrayList(StringConstants.TIME_SLOTS.value)!!
//
//        }
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        getDataFromIntents(savedInstanceState)
//        initClicks()
//        createRadioButton()
//
//    }
//
//    private fun createRadioButton() {
//        groupradio.removeAllViews()
//
//        for (i in 0 until timeSlots.size) {
//            val radioButton: RadioButton =
//                LayoutInflater.from(requireContext()).inflate(R.layout.layout_rb_time_slots, null) as RadioButton
//            radioButton.text = timeSlots[i]
//            radioButton.hint = timeSlots[i]
//            radioButton.id = i + 100
//            groupradio.addView(radioButton)
//
//        }
//    }
//
//    private fun initClicks() {
//        tv_cancel_time_slots.setOnClickListener {
//            this.dismiss()
//        }
//        tv_done_time_slots.setOnClickListener {
//            if (groupradio.checkedRadioButtonId == -1) {
//                Toast.makeText(
//                    requireContext(),
//                    getString(R.string.no_time_slot),
//                    Toast.LENGTH_LONG
//                ).show()
//                return@setOnClickListener
//            }
//
//            val radioButtonID: Int = groupradio.checkedRadioButtonId
//            val radioButton: RadioButton = groupradio.findViewById(radioButtonID)
//            callbacks.setSelectedTimeSlot(radioButton.hint.toString())
//            this.dismiss()
//        }
//    }
//
//    fun setCallbacks(callbacks: TimeSlotDialogCallbacks) {
//        this.callbacks = callbacks
//    }
//
//    public interface TimeSlotDialogCallbacks {
//        fun setSelectedTimeSlot(time: String)
//    }
//
//    companion object {
//
//        fun newInstance(): TimeSlotsDialog {
//            return TimeSlotsDialog()
//        }
//    }
}