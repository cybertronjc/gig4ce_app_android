package com.gigforce.common_ui.viewdatamodels

import com.gigforce.core.datamodels.gigpage.Gig
import com.google.firebase.firestore.DocumentChange

data class DocChange(var type: DocumentChange.Type? = null, var gig: Gig? = null)