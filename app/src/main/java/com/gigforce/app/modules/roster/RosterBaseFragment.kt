package com.gigforce.app.modules.roster

import android.app.Dialog
import android.content.Context
import android.os.Build
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.view.Window
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.activityViewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.roster.models.Gig
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.gigs_today_warning_dialog.*
import kotlinx.android.synthetic.main.reason_for_gig_cancel_dialog.*
import kotlinx.android.synthetic.main.roster_day_fragment.*
import java.time.LocalDate
import java.time.LocalDateTime


abstract class RosterBaseFragment: BaseFragment() {

    val rosterViewModel: RosterDayViewModel by activityViewModels<RosterDayViewModel>()


    val marginCardStart = 95.px
    val marginCardEnd = 16.px

    // TODO: Modify to get this height from dimens
    val itemHeight = 70

    @RequiresApi(Build.VERSION_CODES.O)
    fun isSameDate(compareWith: LocalDateTime, compareTo: LocalDateTime): Boolean {
        return (compareWith.year == compareTo.year) &&
                (compareWith.monthValue == compareTo.monthValue) &&
                (compareWith.dayOfMonth == compareTo.dayOfMonth)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isLessDate(compareWith: LocalDateTime, compareTo: LocalDateTime):Boolean {
        return (compareWith.year < compareTo.year) ||
                ((compareWith.year == compareTo.year) && (compareWith.monthValue < compareTo.monthValue)) ||
                ((compareWith.year == compareTo.year) &&
                        (compareWith.monthValue == compareTo.monthValue) &&
                        (compareWith.dayOfMonth < compareTo.dayOfMonth))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun isMoreDate(compareWith: LocalDateTime, compareTo: LocalDateTime): Boolean {
        return !isLessDate(compareWith, compareTo) && !isSameDate(compareWith, compareTo)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun showGigsTodayWarning(context: Context, upcomingGigs: ArrayList<Gig>, gigParentView: ConstraintLayout) {

        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.gigs_today_warning_dialog)

        dialog.dialog_content.setText(
            "You have " + upcomingGigs.size.toString() + " Gig(s) active on the day. These gigs will get canceled as well."
        )

        dialog.cancel.setOnClickListener {
            dialog .dismiss()
        }

        dialog.yes.setOnClickListener {
            Toast.makeText(requireContext(), "Clicked on Yes", Toast.LENGTH_SHORT).show()
            showReasonForGigCancel(context, upcomingGigs, gigParentView)
            dialog .dismiss()
        }

        dialog.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun showReasonForGigCancel(context: Context, upcomingGigs: ArrayList<Gig>, gigParentView: ConstraintLayout) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.reason_for_gig_cancel_dialog)

        var selectedText = ""

        dialog.cancel_options.setOnCheckedChangeListener ( RadioGroup.OnCheckedChangeListener { group, checkedId ->
            selectedText = dialog.findViewById<RadioButton>(checkedId).text.toString()
        })

        dialog.submit_button.setOnClickListener {
            Toast.makeText(requireContext(), "selected option " + selectedText, Toast.LENGTH_SHORT).show()
            val child = hourview_viewpager.getChildAt(0)

            // removing upcoming gigs
            // TODO: Can take this functioni out
            for (gig in upcomingGigs)
                gigParentView.removeView(gigParentView.findViewWithTag<UpcomingGigCard>(gig.tag))
//                child.findViewWithTag<ConstraintLayout>("day_times").removeView(child.findViewWithTag<UpcomingGigCard>(gig.tag))
            rosterViewModel.isDayAvailable.value = false
            dialog .dismiss()
        }

        dialog.cancel_button.setOnClickListener {
            dialog .dismiss()
        }

        dialog.show()
    }
    open fun setMargins(
        view: View,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int
    ) {
        if (view.getLayoutParams() is MarginLayoutParams) {
            val p = view.getLayoutParams() as MarginLayoutParams
            p.setMargins(left, top, right, bottom)
            view.requestLayout()
        }
    }
}