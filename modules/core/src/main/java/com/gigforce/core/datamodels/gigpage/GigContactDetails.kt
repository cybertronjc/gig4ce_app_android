
package com.gigforce.core.datamodels.gigpage

import androidx.annotation.Keep
import com.google.firebase.firestore.Exclude

@Keep
data class GigContactDetails(
        var contactName: String? = null,
        var contactNumber: Long = 0

) {
    @get:Exclude
    @set:Exclude
    var contactNumberString: String = ""
        get() = contactNumber.toString()

}