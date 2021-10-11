package com.gigforce.core.datamodels

class CommonViewTypes {
    /*
            VIEWTYPE Numbering:
                {Module: 20}
                WITHIN MODULE:
                    {STANDARD: 0} {component-default: 01}
                    {STANDARD: 0} {component-lightpink: 02}
                    {STANDARD: 0} {component-LIPSTICK: 03}
                    {STANDARD: 0} {component-GREY: 04}

                    {VIDEOS: 1} {component: 01}
                    {VIDEOS : 1} {component-ITEM: 02}
                    {FEATURE: 2} {component: 01}
                    {FEATURE: 2} {component-ITEM: 02}

     */
    companion object {
        const val VIEW_STANDARD_ACTION_CARD = 20001

        const val VIEW_VIDEOS_LAYOUT = 20101
        const val VIEW_VIDEOS_ITEM_CARD = 20102

        const val VIEW_FEATURE_LAYOUT = 20201
        const val VIEW_FEATURE_ITEM_CARD = 20202
        const val VIEW_FEATURE_ITEM_CARD2 = 20203
        const val VIEW_GIG_ITEM_CARD = 20204
        const val VIEW_OTHER_FEATURE = 20207
        const val VIEW_OTHER_FEATURE_ITEM = 20208
        const val VIEW_SEE_MORE_ITEM = 20209
        const val VIEW_BANNER_CARD = 20210
        const val VIEW_PENDING_JOINING_ITEM = 202109

        const val VIEW_PROFILE_PIC = 20401

        const val VIEW_VIDEOS_ITEM_CARD2 = 20205
        const val VIEW_ASSESMENT_ITEM_CARD = 20206

        const val VIEW_SIMPLE_CARD = 20301

        //ALL module specific component will be start from 20401. Need to discuss with Himanshu for below layout
        const val VIEW_GIGER_ATTENDANCE = 12234
        const val VIEW_ATTENDANCE_BUSINESS_SHIFT_TIME = 12236
    }
}