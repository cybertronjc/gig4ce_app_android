package com.gigforce.app.modules.roster

import android.os.Build
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.annotation.RequiresApi
import androidx.fragment.app.activityViewModels
import com.gigforce.app.core.base.BaseFragment
import com.google.android.material.bottomsheet.BottomSheetBehavior
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