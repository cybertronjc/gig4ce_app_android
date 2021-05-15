package com.gigforce.giger_gigs.models

import com.gigforce.core.datamodels.gigpage.Gig
import com.google.firebase.firestore.DocumentChange

data class DocChange(var type: DocumentChange.Type? = null, var gig: Gig? = null)