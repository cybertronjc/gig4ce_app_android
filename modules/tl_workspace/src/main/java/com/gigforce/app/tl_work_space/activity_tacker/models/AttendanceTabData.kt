package com.gigforce.app.tl_work_space.activity_tacker.models

import com.gigforce.app.tl_work_space.activity_tacker.attendance_list.GigerAttendanceUnderManagerViewModel
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.app.tl_work_space.retentions.RetentionViewModel


data class AttendanceTabData(
    val id: String,
    val title: String,
    var value: Int,
    var selected: Boolean,
    val valueChangedBy: Int,
    val changeType: ValueChangeType,
    val viewModel: GigerAttendanceUnderManagerViewModel
)
