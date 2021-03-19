package com.gigforce.core.datamodels.gigpage

import com.google.firebase.firestore.DocumentChange

data class DocChange(var type: DocumentChange.Type? = null, var gig: Gig? = null)