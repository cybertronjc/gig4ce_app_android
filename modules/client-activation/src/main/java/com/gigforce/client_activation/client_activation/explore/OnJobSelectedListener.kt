package com.gigforce.client_activation.client_activation.explore

import com.gigforce.client_activation.client_activation.dataviewmodel.JobProfileDVM
import com.gigforce.client_activation.client_activation.models.JpExplore

interface OnJobSelectedListener {

    fun onJobSelected(
        jpExplore: JobProfileDVM
    )
}