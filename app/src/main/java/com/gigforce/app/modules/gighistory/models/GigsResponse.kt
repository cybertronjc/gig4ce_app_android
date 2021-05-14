package com.gigforce.app.modules.gighistory.models

//import com.gigforce.app.core.base.models.BaseResponse
import com.gigforce.giger_gigs.models.Gig
import com.gigforce.core.base.models.BaseResponse


data class GigsResponse(
    override val status: Boolean = false, override val message: String = "",
    override var data: ArrayList<Gig>? = ArrayList()
) : BaseResponse<List<Gig>?>()