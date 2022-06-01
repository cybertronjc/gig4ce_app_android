package com.gigforce.app.data.repositoriesImpl.gigs.models

import com.gigforce.core.extensions.toLocalDateTime
import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.*

data class MarkAttendanceRequest(

	@field:SerializedName("gigId")
	val gigId: String,

	@field:SerializedName("attendance")
	val attendance: String,

	@field:SerializedName("absentReason")
	val absentReason: String? = null,

	@field:SerializedName("absentReasonLocalizedText")
	val absentReasonLocalizedText: String? = null,

	@field:SerializedName("type")
	val type: String? = null,

	@field:SerializedName("imagePathInFirebase")
	val imagePathInFirebase: String? = null,

	@field:SerializedName("latitude")
	val latitude: Double? = null,

	@field:SerializedName("longitude")
	val longitude: Double? = null,

	@field:SerializedName("markingAddress")
	val markingAddress: String? = null,

	@field:SerializedName("locationAccuracy")
	val locationAccuracy: Float? = null,

	@field:SerializedName("locationFake")
	var locationFake: Boolean? = null,

	)