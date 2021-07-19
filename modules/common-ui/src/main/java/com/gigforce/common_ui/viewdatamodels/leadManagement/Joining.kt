package com.gigforce.common_ui.viewdatamodels.leadManagement

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class Joining(

	@DocumentId
	@get:PropertyName("uid")
	@set:PropertyName("uid")
	var uid: String = "",

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

	@get:PropertyName("appInviteSentTimestamp")
	@set:PropertyName("appInviteSentTimestamp")
	var appInviteSentTimestamp:  Timestamp? = null,

	@get:PropertyName("applicationInviteSentTimeStamp")
	@set:PropertyName("applicationInviteSentTimeStamp")
	var applicationInviteSentTimeStamp:  Timestamp? = null,

	@get:PropertyName("applicationNameInvitedFor")
	@set:PropertyName("applicationNameInvitedFor")
	var applicationNameInvitedFor:  String? = null,

	@get:PropertyName("signUpMode")
	@set:PropertyName("signUpMode")
	var signUpMode: String? = null,
) {

	@Exclude
    fun getStatus(): JoiningStatus {
        return JoiningStatus.valueOf(status)
    }
}
