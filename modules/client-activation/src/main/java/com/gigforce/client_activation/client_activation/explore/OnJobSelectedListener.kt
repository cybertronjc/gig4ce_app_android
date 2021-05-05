package com.gigforce.client_activation.client_activation.explore

import com.gigforce.client_activation.client_activation.models.JobProfile

interface OnJobSelectedListener {

    fun onJobSelected(
        jobProfile: JobProfile
    )
}