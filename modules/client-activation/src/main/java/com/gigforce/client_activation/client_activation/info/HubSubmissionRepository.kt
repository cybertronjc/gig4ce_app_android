package com.gigforce.client_activation.client_activation.info

import com.gigforce.core.StringConstants
import com.gigforce.core.datamodels.client_activation.JpApplication
import com.gigforce.core.extensions.getOrThrow
import com.gigforce.core.extensions.setOrThrow
import com.gigforce.core.extensions.updateOrThrow
import com.gigforce.core.userSessionManagement.FirebaseAuthStateListener
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception

class HubSubmissionRepository {
    var uid = FirebaseAuth.getInstance().currentUser?.uid!!

    suspend fun submitHubData(state: String, stateId : String, city:String, cityId : String, hub: String,hubId : String,mJobProfileId: String):JpApplication?{
        try {
            var jpData = FirebaseFirestore.getInstance().collection("JP_Applications")
                .whereEqualTo("jpid", mJobProfileId)
                .whereEqualTo(
                    "gigerId",
                    FirebaseAuthStateListener.getInstance().getCurrentSignInUserInfoOrThrow().uid
                ).getOrThrow()
            val jpApplication =
                jpData.toObjects(
                    JpApplication::class.java
                )[0]
            if(jpData.documents.isNotEmpty()){

                var submissionData = FirebaseFirestore.getInstance().collection("JP_Applications")
                    .document(jpData.documents[0].id).collection("Submissions")
                    .whereEqualTo("type", "hub_location").getOrThrow()
                if(submissionData.documents.isNullOrEmpty()){
                    FirebaseFirestore.getInstance()
                        .collection("JP_Applications")
                        .document(jpApplication.id)
                        .collection("Submissions").document().setOrThrow(
                            mapOf(
                                "hubId" to hubId,
                                "hubName" to hub,
                                "stateName" to state,
                                "stateId" to stateId,
                                "hubCityId" to cityId,
                                "hubCity" to city,
                                "type" to "hub_location"
                            )
                        )
                }else{
                    FirebaseFirestore.getInstance()
                        .collection("JP_Applications")
                        .document(jpApplication.id)
                        .collection("Submissions").document(submissionData.documents[0].id).updateOrThrow(
                            mapOf(
                                "hubId" to hubId,
                                "hubName" to hub,
                                "stateName" to state,
                                "stateId" to stateId,
                                "hubCityId" to cityId,
                                "hubCity" to city,
                                "type" to "hub_location"
                            )
                        )
                }

                FirebaseFirestore.getInstance()
                    .collection("JP_Applications")
                    .document(jpApplication.id).updateOrThrow(mapOf("updatedAt" to Timestamp.now(), "updatedBy" to StringConstants.APP.value))

                return jpApplication
            }
            return null
        }catch (e:Exception){
            return null
        }
    }
}