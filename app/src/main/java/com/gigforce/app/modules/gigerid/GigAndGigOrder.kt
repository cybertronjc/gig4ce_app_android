package com.gigforce.app.modules.gigerid

import com.gigforce.giger_gigs.models.Gig
import com.gigforce.core.datamodels.gigpage.GigOrder

data class GigAndGigOrder (
    val gig: Gig,
    val gigOrder: GigOrder
){

}