package com.gigforce.app.tl_work_space.home.models

data class TLWorkspaceCardItemData(
    val title: String,
    val value: Int,
    val valueChangedBy: Int,
    val changeType: ValueChangeType
) {


    fun hasSameContentAs(
        data: TLWorkspaceCardItemData
    ): Boolean {
        return this.title == data.title &&
                this.value == data.value &&
                this.valueChangedBy == data.valueChangedBy &&
                this.changeType == data.changeType
    }
}