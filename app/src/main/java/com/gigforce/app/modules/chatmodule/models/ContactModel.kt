package com.gigforce.app.modules.chatmodule.models

import com.google.firebase.firestore.DocumentId

data class ContactModel(
 @DocumentId
 var id: String? = null,
 var mobile: String = "",
 var name: String? = null,
 var fb_uid: String? = null,
 var contactId: String? = null,
 var headerId: String? = null,
 var imageUrl : String? = null,
 var date : String? = null,
 var time : String? = null,
 var uid : String? = null,
 var isGigForceUser : Boolean = false
)