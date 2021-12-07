package com.gigforce.core.datamodels.client_activation

import com.gigforce.core.StringConstants
import com.google.firebase.Timestamp
import com.google.gson.annotations.SerializedName
import java.util.*

data class JpApplication(
        var id: String = "",
        var JPId: String = "",
        var approvedBy: String = "",
        var approvedOn: String = "",
        var gigerId: String = "",
        var rejectedBy: String = "",
        var rejectedOn: String = "",
        var status: String = "",
        var submitOn: String = "",
        @SerializedName("application") var application: MutableList<Dependency> = mutableListOf(),
        @SerializedName("activation") var activation: MutableList<Dependency> = mutableListOf(),
        var applicationStart: Date? = Date(),
        var applicationComplete: Date? = null,
        var activationStart: Date? = null,
        var activationComplete: Date? = null,
        var applicationLearningCompletionDate: Date? = null,
        var activationLearningCompletionDate: Date? = null,
        var verifiedTLNumber: String? = null,
        var dateOfJoining: Timestamp? = null,
        var updatedAt: Timestamp? = Timestamp.now(),
        var updatedBy: String? = null,
        var createdAt: Timestamp? = Timestamp.now()

){
        fun setUpdatedAtAndBy(uid : String){
                updatedAt = Timestamp.now()
                updatedBy = uid
        }

        fun setCreatedAt(){
                createdAt = Timestamp.now()
        }
}