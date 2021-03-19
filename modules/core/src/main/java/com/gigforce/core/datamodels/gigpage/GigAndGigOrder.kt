package com.gigforce.core.datamodels.gigpage

import com.gigforce.core.datamodels.gigpage.Gig

data class GigAndGigOrder(
        val gig: Gig,
        val gigOrder: GigOrder
)