package com.gigforce.modules.feature_chat.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class ContactModel(

 @get:PropertyName("id")
 @set:PropertyName("id")
 var id: String? = null,

 @get:PropertyName("uid")
 @set:PropertyName("uid")
 var uid: String? = null,

 @get:PropertyName("mobile")
 @set:PropertyName("mobile")
 var mobile: String = "",

 @get:PropertyName("name")
 @set:PropertyName("name")
 var name: String? = null,

 @get:PropertyName("contactId")
 @set:PropertyName("contactId")
 var contactId: String? = null,

 @get:PropertyName("headerId")
 @set:PropertyName("headerId")
 var headerId: String? = null,

 @get:PropertyName("imagePathInStorage")
 @set:PropertyName("imagePathInStorage")
 var imagePathInStorage: String? = null,

 @get:PropertyName("imageUrl")
 @set:PropertyName("imageUrl")
 var imageUrl: String? = null,

 @get:PropertyName("createdOn")
 @set:PropertyName("createdOn")
 var createdOn: Timestamp = Timestamp.now(),

 @get:PropertyName("updatedOn")
 @set:PropertyName("updatedOn")
 var updatedOn: Timestamp = Timestamp.now(),

 @get:PropertyName("isGigForceUser")
 @set:PropertyName("isGigForceUser")
 var isGigForceUser: Boolean = false,

 @get:PropertyName("isUserGroupManager")
 @set:PropertyName("isUserGroupManager")
 var isUserGroupManager: Boolean = false
)