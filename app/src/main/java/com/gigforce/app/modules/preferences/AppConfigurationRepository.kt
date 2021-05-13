package com.gigforce.app.modules.preferences

import com.gigforce.app.modules.ambassador_user_enrollment.models.City
import com.gigforce.app.modules.ambassador_user_enrollment.models.State
import com.gigforce.app.modules.profile.models.Skill
import com.gigforce.app.modules.profile.models.Skill2
import com.gigforce.app.utils.getOrThrow
import com.gigforce.profile.models.SkillsDetails
import com.gigforce.profile.onboarding.models.SkillModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AppConfigurationRepository constructor(
    private val firebaseFireStore : FirebaseFirestore = FirebaseFirestore.getInstance()
)  {

    companion object{
        const val COLLECTION_NAME = "Configuration"

        const val DOCUMENT_LANGUAGE = "Languages"
    }

    suspend fun getActiveLanguages() : List<String>  = suspendCoroutine {cont ->
        firebaseFireStore
            .collection(COLLECTION_NAME)
            .document(DOCUMENT_LANGUAGE)
            .get()
            .addOnSuccessListener {

               val activeLanguages =  it.get("activeLanguages") as List<String>
                cont.resume(activeLanguages)
            }
            .addOnFailureListener {
                cont.resumeWithException(it)
            }
    }


    suspend fun getStates() : List<State>{
       val statesQuery =  firebaseFireStore
            .collection("Mst_States")
            .getOrThrow()

       return statesQuery.documents.map {
            it.toObject(State::class.java)!!.apply {
                this.id = it.id
            }
        }
    }

    suspend fun getCities() : List<City>{
        val citiesQuery =  firebaseFireStore
            .collection("Mst_Cities")
            .getOrThrow()

        return citiesQuery.documents.map {
            it.toObject(City::class.java)!!.apply {
                this.id = it.id
            }
        }
    }

    suspend fun getSkillsForAmbassadorEnrollment() : List<Skill2>{
        val ambassadorSkillsQuery =  firebaseFireStore
                .collection("Mst_Skills")
                .whereEqualTo("shouldShowInAmbassasdorSkillsScreen", true)
                .getOrThrow()

        return ambassadorSkillsQuery.documents.map {
            it.toObject(Skill2::class.java)!!.apply {
                this.id = it.id
            }
        }
    }

}