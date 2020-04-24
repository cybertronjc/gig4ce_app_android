package com.gigforce.app.modules.roster

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.view.marginTop
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.modules.roster.adapters.TimeAdapter
import com.google.type.Color
import kotlinx.android.synthetic.main.roaster_day_fragment.*
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList

class RosterDayFragment: RosterBaseFragment() {

    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var timeAdapter: TimeAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.roaster_day_fragment, inflater, container)

        return getFragmentView()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        linearLayoutManager = LinearLayoutManager(this.context)
        day_times.layoutManager = linearLayoutManager

        var times = ArrayList<String>()
        times.addAll(listOf("12:00", "01:00", "02:00", "03:00", "04:00", "05:00", "06:00", "07:00", "08:00", "09:00",
            "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00",
            "21:00", "22:00", "23:00"))

        var datetime = LocalDateTime.now()
        Log.d("DAY", datetime.toString() + " " + datetime.hour + " " + datetime.minute)

        timeAdapter = TimeAdapter(times)
        day_times.adapter = timeAdapter

        Log.d("ROSTER", timeAdapter.itemCount.toString())

    }
}