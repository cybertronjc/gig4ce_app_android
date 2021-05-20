package com.gigforce.giger_gigs.gigerid

import com.gigforce.core.datamodels.gigpage.Gig
import com.gigforce.core.datamodels.gigpage.GigOrder

data class GigAndGigOrder (
    val gig: Gig,
    val gigOrder: GigOrder
){

}