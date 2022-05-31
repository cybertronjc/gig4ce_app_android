package com.gigforce.app.tl_work_space.custom_tab

import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.core.SimpleDVM

data class CustomTabData(
    val id: String,
    val title: String,
    var value: Int,
    val selected : Boolean,
    val valueChangedBy: Int,
    val changeType: ValueChangeType,
    val viewModel: CustomTabClickListener
) : SimpleDVM(TLWorkSpaceCoreRecyclerViewBindings.CustomTabType1)