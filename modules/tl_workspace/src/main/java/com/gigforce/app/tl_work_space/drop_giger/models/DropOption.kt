package com.gigforce.app.tl_work_space.drop_giger.models

import androidx.lifecycle.ViewModel
import com.gigforce.app.tl_work_space.TLWorkSpaceCoreRecyclerViewBindings
import com.gigforce.app.tl_work_space.drop_giger.DropGigerViewModel
import com.gigforce.core.SimpleDVM

data class DropOption(
    var dropLocalizedText : String,
    var reasonId : String,
    val customReason : Boolean,
    var selected : Boolean,
    val viewModel: DropGigerViewModel
) : SimpleDVM(TLWorkSpaceCoreRecyclerViewBindings.DropGigerItemType)
