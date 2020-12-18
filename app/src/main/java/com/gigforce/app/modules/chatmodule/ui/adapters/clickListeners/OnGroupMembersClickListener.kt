package com.gigforce.app.modules.chatmodule.ui.adapters.clickListeners

import android.view.View
import com.gigforce.app.modules.chatmodule.models.ContactModel

interface OnGroupMembersClickListener {

    fun onGroupMemberItemLongPressed(
        view: View,
        position: Int,
        contact: ContactModel
    )

    fun onChatIconClicked(
        position: Int,
        contact: ContactModel
    )
}