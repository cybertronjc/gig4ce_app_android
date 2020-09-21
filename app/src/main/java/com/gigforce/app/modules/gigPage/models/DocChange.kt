package com.gigforce.app.modules.gigPage.models

import com.google.firebase.firestore.DocumentChange

data class DocChange(var type: DocumentChange.Type? = null, var gig: Gig? = null)