package com.gigforce.app.modules.gigerid

import com.gigforce.app.modules.gigPage2.models.Gig
import com.gigforce.core.datamodels.gigpage.GigOrder

data class GigAndGigOrder (
    val gig: Gig,
    val gigOrder: GigOrder
){

}