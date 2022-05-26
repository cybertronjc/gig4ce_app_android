package com.gigforce.app.tl_work_space.home.views

import com.gigforce.app.tl_work_space.R
import java.io.Serializable

class ActionsAttachmentOption(val id: Int, val title: String, val resourceImage: Int) : Serializable {


    companion object {
        const val SELECTION_FORM_ID: Int = 101
        const val LOGIN_SUMMARY_ID: Int = 102
        const val RAISE_GIGER_TICKET_ID: Int = 103
        const val GIGER_ATTENDANCE_ID: Int = 104
        const val ALL_SELECTIONS_ID: Int = 105
        const val COMPLIANCE_PENDING_ID: Int = 106
        const val GIGER_PAYOUT_ID: Int = 107
        const val GIGER_RETENTION_ID: Int = 108
        const val GIGER_TICKET_ID: Int = 109
        val quickOptionsList: List<ActionsAttachmentOption>
            get() {
                val attachmentOptions: MutableList<ActionsAttachmentOption> = ArrayList()
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        SELECTION_FORM_ID.toInt(),
                        "Selection\n" + "Form",
                        R.drawable.ic_action_selection_form
                    )
                )
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        LOGIN_SUMMARY_ID.toInt(),
                        "Login\n" + "Summary",
                        R.drawable.ic_action_login_summary
                    )
                )
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        RAISE_GIGER_TICKET_ID.toInt(),
                        "Raise\n" + " Giger Ticket",
                        R.drawable.ic_action_raise_ticket
                    )
                )
                return attachmentOptions
            }

        val allOptionsList: List<ActionsAttachmentOption>
            get() {
                val attachmentOptions: MutableList<ActionsAttachmentOption> = ArrayList()
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        GIGER_ATTENDANCE_ID.toInt(),
                        "Giger\n" + " Attendance",
                        R.drawable.ic_action_giger_attandence
                    )
                )
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        ALL_SELECTIONS_ID.toInt(),
                        "All\n" + " Selections ",
                        R.drawable.ic_action_all_selections
                    )
                )
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        COMPLIANCE_PENDING_ID.toInt(),
                        "Compliance\n" + " Pending",
                        R.drawable.ic_action_compliance_pending
                    )
                )
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        GIGER_PAYOUT_ID.toInt(),
                        "Giger\n" + " Payout",
                        R.drawable.ic_action_giger_payout
                    )
                )
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        GIGER_RETENTION_ID.toInt(),
                        "Giger\n" + " Retention",
                        R.drawable.ic_action_giger_retention
                    )
                )
                attachmentOptions.add(
                    ActionsAttachmentOption(
                        GIGER_TICKET_ID.toInt(),
                        "Giger\n" + " Tickets",
                        R.drawable.ic_action_giger_ticket
                    )
                )
                return attachmentOptions
            }
    }
}