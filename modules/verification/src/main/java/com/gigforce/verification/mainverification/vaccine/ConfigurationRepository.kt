package com.gigforce.verification.mainverification.vaccine


import com.gigforce.core.extensions.getOrThrow

import com.gigforce.verification.mainverification.vaccine.models.VaccineConfigListDM

import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class ConfigurationRepository @Inject constructor(){
    suspend fun getVaccineConfigData(): VaccineConfigListDM {
                val vaccineList =
                        FirebaseFirestore.getInstance().collection("Configuration").document("vaccine_list")
                        .getOrThrow()
                return vaccineList.toObject(VaccineConfigListDM::class.java)
                    ?: VaccineConfigListDM(ArrayList())
            }
}