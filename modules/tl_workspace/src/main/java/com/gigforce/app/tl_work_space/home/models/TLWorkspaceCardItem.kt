package com.gigforce.app.tl_work_space.home.models

data class TLWorkspaceCardItem(
    val title: String,
    val value: Int,
    val valueChangedBy: Int,
    val changeType: ValueChangeType
)
