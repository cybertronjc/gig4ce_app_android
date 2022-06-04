package com.gigforce.app.domain.models.tl_workspace.retention

import com.gigforce.app.domain.models.tl_workspace.FiltersItemApiModel
import com.google.gson.annotations.SerializedName

data class GetRetentionResponse(

	@field:SerializedName("gigersRetentionList")
	val gigersRetentionList: List<GigersRetentionListItem>? = null,

	@field:SerializedName("statusMasterWithCount")
	val statusMasterWithCount: List<StatusMasterWithCountItem>? = null,

	@field:SerializedName("filters")
	val filters: List<FiltersItemApiModel>? = null
)

data class StatusMasterWithCountItem(

	@field:SerializedName("valueChangeType", alternate = arrayOf("countChangeType"))
	val valueChangeType: String? = null,

	@field:SerializedName("count")
	val count: Int? = null,

	@field:SerializedName("id", alternate = arrayOf("cardId"))
	val id: String? = null,

	@field:SerializedName("valueChangedBy", alternate = arrayOf("countChangedBy"))
	val valueChangedBy: Int? = null,

	@field:SerializedName("title")
	val title: String? = null
)

data class GigersRetentionListItem(

	@field:SerializedName("profilePicture")
	val profilePicture: String? = null,

	@field:SerializedName("lastActiveString")
	val lastActiveString: String? = null,

	@field:SerializedName("warningString")
	val warningString: String? = null,

	@field:SerializedName("jobProfile")
	val jobProfile: String? = null,

	@field:SerializedName("business")
	val business: String? = null,

	@field:SerializedName("gigerId")
	val gigerId: String? = null,

	@field:SerializedName("mobileNumber")
	val mobileNumber: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("profilePictureThumbnail")
	val profilePictureThumbnail: String? = null,

	@field:SerializedName("reation")
	val tabStatus : List<String>? = null
){

	fun getBusinessNonNull() : String{
		return business ?: "Other"
	}
}
