package com.gigforce.common_ui.core

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
    companion object{
        const val VIEW_STANDARD_ACTION_CARD = 20001
        const val VIEW_STANDARD_LIGHT_PINK = 20002
        const val VIEW_STANDARD_LIPSTICK = 20003
        const val VIEW_STANDARD_GREY = 20004
        const val VIEW_STANDARD_LIGHT_BLUE_WITH_MARGIN = 20005


        const val VIEW_VIDEOS_LAYOUT = 20101
        const val VIEW_VIDEOS_ITEM_CARD = 20102

        const val VIEW_FEATURE_LAYOUT = 20201
        const val VIEW_FEATURE_ITEM_CARD = 20202

        const val VIEW_TEXT_OUTLINE_BUTTON = 20301

        const val VIEW_PROFILE_PIC = 20401
    }
}