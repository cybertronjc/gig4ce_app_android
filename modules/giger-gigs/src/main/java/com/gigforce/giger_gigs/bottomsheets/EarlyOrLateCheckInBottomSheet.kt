package com.gigforce.giger_gigs.bottomsheets

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.giger_gigs.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_gig_early_or_late_check_in.*
import java.text.SimpleDateFormat
import java.util.*

class EarlyOrLateCheckInBottomSheet : BottomSheetDialogFragment() {

    private var checkInType: String? = null
    private var actualCheckInTime: String? = null
    private var checkInTimeAccToUser: Date? = null
    var onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener? = null

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
            your_time_tv?.text = timeFormatter.format(newCal.time)
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
        dialog?.setCanceledOnTouchOutside(false)
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
                title_bar.text = getString(R.string.early_checkin_giger_gigs)
                actual_checkin_time_tv.text = getString(R.string.gig_checkin_giger_gigs)
                check_in_label.text = getString(R.string.checking_in_early_giger_gigs)

                your_checkin_layout.gone()
            }
            CHECK_IN_TYPE_LATE -> {
                title_bar.text = getString(R.string.late_checkin_giger_gigs)
                actual_checkin_time_tv.text = getString(R.string.gig_checkin_giger_gigs)
                your_checkin_time_tv.text = getString(R.string.your_checkin_giger_gigs)
                check_in_label.text = getString(R.string.checking_in_late_giger_gigs)

                your_checkin_layout.visible()
            }
            CHECK_OUT_TYPE_EARLY -> {
                title_bar.text = getString(R.string.early_checkout_giger_gigs)
                actual_checkin_time_tv.text = getString(R.string.gig_checkout_giger_gigs)

                check_in_label.text = getString(R.string.checking_out_early_giger_gigs)
                your_checkin_layout.gone()
            }
            CHECK_OUT_TYPE_LATE -> {
                title_bar.text = getString(R.string.late_checkout_giger_gigs)
                actual_checkin_time_tv.text = getString(R.string.gig_checkout_giger_gigs)
                your_checkin_time_tv.text = getString(R.string.your_checkin_giger_gigs)

                check_in_label.text = getString(R.string.checking_out_late_notified_giger_gigs)
                your_checkin_layout.visible()
            }
            else -> {
            }
        }
    }

    companion object {
        const val TAG = "EarlyOrLateCheckInBottomSheet"

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

                this.isCancelable = false
                val args = bundleOf(
                        INTENT_CHECK_IN_TYPE to CHECK_IN_TYPE_EARLY,
                        INTENT_ACTUAL_CHECKIN_TIME to actualCheckInTime
                )
                this.arguments = args
                this.onEarlyOrLateCheckInBottomSheetClickListener =
                        onEarlyOrLateCheckInBottomSheetClickListener

                show(childFragmentMgr, TAG)
            }
        }

        fun launchLateCheckInBottomSheet(
                childFragmentMgr: FragmentManager,
                actualCheckInTime: String?,
                onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener
        ) {
            EarlyOrLateCheckInBottomSheet().apply {

                this.isCancelable = false
                val args = bundleOf(
                        INTENT_CHECK_IN_TYPE to CHECK_IN_TYPE_LATE,
                        INTENT_ACTUAL_CHECKIN_TIME to actualCheckInTime
                )
                this.arguments = args
                this.onEarlyOrLateCheckInBottomSheetClickListener =
                        onEarlyOrLateCheckInBottomSheetClickListener

                show(childFragmentMgr, TAG)
            }
        }


        fun launchEarlyCheckOutBottomSheet(
                childFragmentMgr: FragmentManager,
                actualCheckInTime: String?,
                onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener
        ) {
            EarlyOrLateCheckInBottomSheet().apply {

                this.isCancelable = false
                val args = bundleOf(
                        INTENT_CHECK_IN_TYPE to CHECK_OUT_TYPE_EARLY,
                        INTENT_ACTUAL_CHECKIN_TIME to actualCheckInTime
                )
                this.arguments = args
                this.onEarlyOrLateCheckInBottomSheetClickListener =
                        onEarlyOrLateCheckInBottomSheetClickListener

                show(childFragmentMgr, TAG)
            }
        }

        fun launchLateCheckOutBottomSheet(
                childFragmentMgr: FragmentManager,
                actualCheckInTime: String?,
                onEarlyOrLateCheckInBottomSheetClickListener: OnEarlyOrLateCheckInBottomSheetClickListener
        ) {
            EarlyOrLateCheckInBottomSheet().apply {

                this.isCancelable = false
                val args = bundleOf(
                        INTENT_CHECK_IN_TYPE to CHECK_OUT_TYPE_LATE,
                        INTENT_ACTUAL_CHECKIN_TIME to actualCheckInTime
                )
                this.arguments = args
                this.onEarlyOrLateCheckInBottomSheetClickListener =
                        onEarlyOrLateCheckInBottomSheetClickListener

                show(childFragmentMgr, TAG)
            }
        }
    }

    interface OnEarlyOrLateCheckInBottomSheetClickListener {

        fun onCheckInOkayClicked(
                checkInOrCheckOutTimeAccToUser: Date?
        )
    }
}