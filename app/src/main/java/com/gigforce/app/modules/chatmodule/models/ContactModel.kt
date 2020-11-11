package com.gigforce.app.modules.chatmodule.models

import com.google.firebase.firestore.DocumentId

data class ContactModel(
 @DocumentId
 val id: String?,
 val mobile: String,
 val name: String? = null,
 val fb_uid: String? = null,
 val contactId: String? = null,
 val headerId: String? = null,
 val imageUrl : String? = null,
 val date : String? = null,
 val time : String? = null,
 val isGigForceUser : Boolean = false
){

}