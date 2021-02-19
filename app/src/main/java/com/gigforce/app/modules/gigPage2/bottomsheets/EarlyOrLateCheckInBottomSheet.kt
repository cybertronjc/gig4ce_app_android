package com.gigforce.app.modules.gigPage2.bottomsheets

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_gig_early_or_late_check_in.*
import java.text.SimpleDateFormat
import java.util.*


class EarlyOrLateCheckInBottomSheet : BottomSheetDialogFragment() {

    private var checkInType: String? = null
    private var actualCheckInTime: String? = null
    private var onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener? = null
    private var checkInTimeAccToUser: Date? = null

    private val timeFormatter = SimpleDateFormat("hh.mm aa", Locale.getDefault())

    private val fromTimePicker: TimePickerDialog by lazy {

        val cal = Calendar.getInstance()
        TimePickerDialog(requireContext(), { _, hourOfDay, minute ->

            val newCal = Calendar.getInstance()
            newCal.set(Calendar.HOUR_OF_DAY, hourOfDay)
            newCal.set(Calendar.MINUTE, minute)
            newCal.set(Calendar.SECOND, 0)
            newCal.set(Calendar.MILLISECOND, 0)

            checkInTimeAccToUser = newCal.time
            your_time_tv.text = timeFormatter.format(newCal.time)
        },
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                false
        )
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_gig_early_or_late_check_in, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(arguments, savedInstanceState)

        initView()
    }

    private fun getDataFromIntents(
            arguments: Bundle?,
            savedInstanceState: Bundle?
    ) {

        arguments?.let {
            checkInType = it.getString(INTENT_CHECK_IN_TYPE)
            actualCheckInTime = it.getString(INTENT_ACTUAL_CHECKIN_TIME)
        }

        savedInstanceState?.let {
            checkInType = it.getString(INTENT_CHECK_IN_TYPE)
            actualCheckInTime = it.getString(INTENT_ACTUAL_CHECKIN_TIME)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_CHECK_IN_TYPE, checkInType)
        outState.putString(INTENT_ACTUAL_CHECKIN_TIME, actualCheckInTime)
    }


    private fun initView() {

        your_time_tv.text = timeFormatter.format(Date())
        okay_btn.setOnClickListener {
            dismiss()
            onEarlyOrLateCheckInBottomSheetClickListener?.onCheckInOkayClicked(
                    checkInTimeAccToUser
            )
        }

        if (actualCheckInTime != null) {
            acutal_checkin_layout.visible()
            checkin_time_tv.text = actualCheckInTime
        } else {
            acutal_checkin_layout.gone()
        }

        your_checkin_layout.setOnClickListener {
            fromTimePicker.show()
        }

        when (checkInType) {
            CHECK_IN_TYPE_EARLY -> {
                title_bar.text = "Early Checkin"
                actual_checkin_time_tv.text = "Gig Check-in"
                check_in_label.text = "You have early checked- in for this gig."

                your_checkin_layout.gone()
            }
            CHECK_IN_TYPE_LATE -> {
                title_bar.text = "Late Checkin"
                actual_checkin_time_tv.text = "Gig Check-in"
                your_checkin_time_tv.text = "Your Check-in"
                check_in_label.text = "This Check- in will be considered once your manager approves."

                your_checkin_layout.visible()
            }
            CHECK_OUT_TYPE_EARLY -> {
                title_bar.text = "Early Checkout"
                actual_checkin_time_tv.text = "Gig Check-out"

                check_in_label.text = "This Check- out will be considered once your manager approves."
                your_checkin_layout.gone()
            }
            CHECK_OUT_TYPE_LATE -> {
                title_bar.text = "Late Checkout"
                actual_checkin_time_tv.text = "Gig Check-out"
                your_checkin_time_tv.text = "Your Check-out"

                check_in_label.text = "This Check- out will be considered once your manager approves."
                your_checkin_layout.visible()
            }
            else -> {
            }
        }
    }

    companion object {
        const val INTENT_CHECK_IN_TYPE = "check_in_type"
        const val INTENT_ACTUAL_CHECKIN_TIME = "actual_checkin_time"

        const val CHECK_IN_TYPE_EARLY = "check_in_early"
        const val CHECK_IN_TYPE_LATE = "check_in_late"
        const val CHECK_OUT_TYPE_EARLY = "check_out_early"
        const val CHECK_OUT_TYPE_LATE = "check_out_late"

        fun launchEarlyCheckInBottomSheet(
                childFragmentMgr: FragmentManager,
                actualCheckInTime: String?,
                onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener
        ) {
            EarlyOrLateCheckInBottomSheet().apply {

                val args = bundleOf(
                        INTENT_CHECK_IN_TYPE to CHECK_IN_TYPE_EARLY,
                        INTENT_ACTUAL_CHECKIN_TIME to actualCheckInTime
                )
                this.arguments = args
                this.onEarlyOrLateCheckInBottomSheetClickListener =
                        onEarlyOrLateCheckInBottomSheetClickListener

                show(childFragmentMgr, "EarlyOrLateCheckInBottomSheet")
            }
        }

        fun launchLateCheckInBottomSheet(
                childFragmentMgr: FragmentManager,
                actualCheckInTime: String?,
                onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener
        ) {
            EarlyOrLateCheckInBottomSheet().apply {

                val args = bundleOf(
                        INTENT_CHECK_IN_TYPE to CHECK_IN_TYPE_LATE,
                        INTENT_ACTUAL_CHECKIN_TIME to actualCheckInTime
                )
                this.arguments = args
                this.onEarlyOrLateCheckInBottomSheetClickListener =
                        onEarlyOrLateCheckInBottomSheetClickListener

                show(childFragmentMgr, "EarlyOrLateCheckInBottomSheet")
            }
        }


        fun launchEarlyCheckOutBottomSheet(
                childFragmentMgr: FragmentManager,
                actualCheckInTime: String?,
                onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener
        ) {
            EarlyOrLateCheckInBottomSheet().apply {

                val args = bundleOf(
                        INTENT_CHECK_IN_TYPE to CHECK_OUT_TYPE_EARLY,
                        INTENT_ACTUAL_CHECKIN_TIME to actualCheckInTime
                )
                this.arguments = args
                this.onEarlyOrLateCheckInBottomSheetClickListener =
                        onEarlyOrLateCheckInBottomSheetClickListener

                show(childFragmentMgr, "EarlyOrLateCheckInBottomSheet")
            }
        }

        fun launchLateCheckOutBottomSheet(
                childFragmentMgr: FragmentManager,
                actualCheckInTime: String?,
                onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener
        ) {
            EarlyOrLateCheckInBottomSheet().apply {

                val args = bundleOf(
                        INTENT_CHECK_IN_TYPE to CHECK_OUT_TYPE_LATE,
                        INTENT_ACTUAL_CHECKIN_TIME to actualCheckInTime
                )
                this.arguments = args
                this.onEarlyOrLateCheckInBottomSheetClickListener =
                        onEarlyOrLateCheckInBottomSheetClickListener

                show(childFragmentMgr, "EarlyOrLateCheckInBottomSheet")
            }
        }
    }

    interface OnEarlyOrLateCheckInBottomSheetClickListener {

        fun onCheckInOkayClicked(
                checkInOrCheckOutTimeAccToUser: Date?
        )
    }
}