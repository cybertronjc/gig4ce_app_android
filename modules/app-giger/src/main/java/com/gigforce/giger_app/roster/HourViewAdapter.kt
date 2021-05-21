package com.gigforce.giger_app.roster

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import java.time.LocalDateTime

class HourViewAdapter(
    activity: FragmentActivity,
    val itemsCount: Int,
    val actualDateTime: LocalDateTime
) : FragmentStateAdapter(activity) {
    var actualDatePosition = 5000

    override fun getItemCount(): Int {
        return itemsCount
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun createFragment(position: Int): Fragment {
        var activeDateTime: LocalDateTime
        if (position > actualDatePosition) {
            //future
            var dayGap = position - actualDatePosition
            activeDateTime = actualDateTime.plusDays(dayGap.toLong())
        } else {
            //past
            var dayGap = actualDatePosition - position
            activeDateTime = actualDateTime.minusDays(dayGap.toLong())
        }

        return HourViewFragment.getInstance(position, activeDateTime)
    }
}