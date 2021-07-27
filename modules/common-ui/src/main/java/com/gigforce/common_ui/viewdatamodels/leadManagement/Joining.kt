package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Joining(

	@get:PropertyName("joiningId")
	@set:PropertyName("joiningId")
	var joiningId: String = "",

	@get:PropertyName("uid")
	@set:PropertyName("uid")
	var uid: String? = null,

	@get:PropertyName("joiningStartedOn")
	@set:PropertyName("joiningStartedOn")
	var joiningStartedOn: Timestamp = Timestamp.now(),

	@get:PropertyName("updatedOn")
	@set:PropertyName("updatedOn")
	var updatedOn: Timestamp = Timestamp.now(),

	@get:PropertyName("joiningTLUid")
	@set:PropertyName("joiningTLUid")
	var joiningTLUid: String = "",

	@get:PropertyName("status")
	@set:PropertyName("status")
	var status: String = "",

	@get:PropertyName("name")
	@set:PropertyName("name")
	var name: String? = null,

	@get:PropertyName("phoneNumber")
	@set:PropertyName("phoneNumber")
	var phoneNumber: String? = null,

	@get:PropertyName("profilePicture")
	@set:PropertyName("profilePicture")
	var profilePicture: String? = null,

	@get:PropertyName("profilePictureThumbnail")
	@set:PropertyName("profilePictureThumbnail")
	var profilePictureThumbnail: String? = null,

	@get:PropertyName("jobProfileIdInvitedFor")
	@set:PropertyName("jobProfileIdInvitedFor")
	var jobProfileIdInvitedFor:  String? = null,

	@get:PropertyName("tradeName")
	@set:PropertyName("tradeName")
	var tradeName: String,

	@get:PropertyName("jobProfileNameInvitedFor")
	@set:PropertyName("jobProfileNameInvitedFor")
	var jobProfileNameInvitedFor:  String? = null,

	@get:PropertyName("jobProfileIcon")
	@set:PropertyName("jobProfileIcon")
	var jobProfileIcon:  String? = null,

	@get:PropertyName("signUpMode")
	@set:PropertyName("signUpMode")
	var signUpMode: String? = null,

	@get:PropertyName("lastStatusChangeSource")
	@set:PropertyName("lastStatusChangeSource")
	var lastStatusChangeSource: String? = null,
) {

	@Exclude
    fun getStatus(): JoiningStatus {
		return JoiningStatus.fromValue(status)
    }
}
