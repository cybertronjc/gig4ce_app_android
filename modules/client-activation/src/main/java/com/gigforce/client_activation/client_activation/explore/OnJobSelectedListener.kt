package com.gigforce.client_activation.client_activation.explore

import com.gigforce.client_activation.client_activation.models.JobProfile
import com.gigforce.client_activation.client_activation.models.JpExplore

interface OnJobSelectedListener {

    fun onJobSelected(
        jpExplore: JpExplore
    )
}