package com.gigforce.app.tl_work_space.retentions.models

import com.gigforce.app.tl_work_space.home.models.ValueChangeType
import com.gigforce.app.tl_work_space.retentions.RetentionViewModel

data class RetentionTabData(
    val tabId: String,
    val tabTitle: String,
    val selected: Boolean,
    val count: Int,
    val valueChangedBy: Int,
    val changeType: ValueChangeType,
    val viewModel: RetentionViewModel
)