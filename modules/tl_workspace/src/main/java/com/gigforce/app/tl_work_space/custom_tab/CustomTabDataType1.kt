package com.gigforce.app.tl_work_space.custom_tab

import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.core.SimpleDVM

open class CustomTabData(
    val tabId : String,
    val type : Int,
    val viewModel: CustomTabClickListener
) : SimpleDVM(type)

data class CustomTabDataType1(
    val id: String,
    val title: String,
    var value: Int,
    val selected : Boolean,
    val valueChangedBy: Int,
    val changeType: ValueChangeType,
    val tabClickListener: CustomTabClickListener
) : CustomTabData(
    tabId = id,
    type = TLWorkSpaceCoreRecyclerViewBindings.CustomTabType1,
    viewModel = tabClickListener
)


data class CustomTabDataType2(
    val id: String,
    val title: String,
    var value: Int,
    val selected : Boolean,
    val tabClickListener: CustomTabClickListener
) : CustomTabData(
    tabId = id,
    type = TLWorkSpaceCoreRecyclerViewBindings.CustomTabType2,
    viewModel = tabClickListener
)