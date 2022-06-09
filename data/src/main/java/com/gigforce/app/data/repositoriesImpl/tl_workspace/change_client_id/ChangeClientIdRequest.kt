package com.gigforce.app.data.repositoriesImpl.tl_workspace.change_client_id

import com.google.gson.annotations.SerializedName

data class ChangeClientIdRequest(

	@field:SerializedName("data")
	val data: List<DataItem>,

	@field:SerializedName("clientIdUpdateLog")
	val clientIdUpdateLog: ClientIdUpdateLog,

	@field:SerializedName("filters")
	val filters: Filters
)

data class ClientIdUpdateLog(

	@field:SerializedName("updatedBy")
	val updatedBy: String? = null,

	@field:SerializedName("source")
	val source: String? = null
)

data class Filters(

	@field:SerializedName("businessId")
	val businessId: String? = null,

	@field:SerializedName("jobProfileId")
	val jobProfileId: String? = null
)

data class DataItem(

	@field:SerializedName("Client Id")
	val clientId: String? = null,

	@field:SerializedName("Job Profile")
	val jobProfile: String? = null,

	@field:SerializedName("Giger Name")
	val gigerName: String? = null,

	@field:SerializedName("Giger Mobile")
	val gigerMobile: String? = null,

	@field:SerializedName("Giger Id")
	val gigerId: String? = null
)
