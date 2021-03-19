package com.gigforce.app.modules.earn.gighistory.models

import com.gigforce.core.base.models.BaseResponse
import com.gigforce.core.datamodels.gigpage.Gig


data class GigsResponse(
    override val status: Boolean = false, override val message: String = "",
    override var data: ArrayList<Gig>? = ArrayList()
) : BaseResponse<List<Gig>?>()