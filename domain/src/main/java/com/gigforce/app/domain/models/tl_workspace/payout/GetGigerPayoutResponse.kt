package com.gigforce.app.domain.models.tl_workspace.payout
import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.google.gson.annotations.SerializedName

data class GetGigerPayoutResponse(

    @field:SerializedName("gigersWithPayoutData")
    val gigersWithPayoutData: List<GigerPayoutListItem>? = null,

    @field:SerializedName("pendingTypeMaster")
    val pendingTypeMaster: List<StatusMasterWithCountItem>? = null,

    @field:SerializedName("filters")
    val filters: List<FiltersItemApiModel>? = null
)

data class StatusMasterWithCountItem(

    @field:SerializedName("countChangeType")
    val countChangeType: String? = null,

    @field:SerializedName("count")
    val count: Int? = null,

    @field:SerializedName("id", alternate = arrayOf("cardId"))
    val id: String? = null,

    @field:SerializedName("countChangedBy")
    val countChangedBy: Int? = null,

    @field:SerializedName("title")
    val title: String? = null
)

data class GigerPayoutListItem(

    @field:SerializedName("businessName")
    val businessName: String? = null,

    @field:SerializedName("gigerId")
    val gigerId: String? = null,

    @field:SerializedName("warningString")
    val warningString: String? = null,

    @field:SerializedName("jobProfile")
    val jobProfile: String? = null,

    @field:SerializedName("mobileNumber")
    val mobileNumber: String? = null,

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("profilePicture")
    val profilePicture: String? = null,

    @field:SerializedName("profilePictureThumbnail")
    val profilePictureThumbnail: String? = null,

    @field:SerializedName("amount")
    val amount: String? = null,

    @field:SerializedName("category")
    val category: String? = null,

    @field:SerializedName("rejectionReason")
    val rejectionReason: String? = null,

    @field:SerializedName("statusColorCode")
    val statusColorCode: String? = null,

    @field:SerializedName("paidOnDate")
    val paidOnDate: String? = null,

    @field:SerializedName("lastPaidOnDate")
    val lastPaidOnDate: String? = null,

    @field:SerializedName("paidOnString")
    val paidOnString: String? = null,

    @field:SerializedName("reation")
    val tabStatus : List<String>? = null
){

    fun getBusinessNonNull() : String{
        return businessName ?: "Other"
    }
}
