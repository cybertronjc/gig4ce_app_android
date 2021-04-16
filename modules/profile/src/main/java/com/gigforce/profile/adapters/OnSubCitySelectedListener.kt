package com.gigforce.profile.adapters

import com.gigforce.profile.models.City

interface OnSubCitySelectedListener {
        fun onSubCitySelected(
            add: Boolean,
            text : String
        )

}