package com.gigforce.verification.mainverification.vaccine

import com.gigforce.verification.mainverification.VerificationKycRepo
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import javax.inject.Inject

class IntermediateVaccinationRepo @Inject constructor(
    val verificationKycRepo: VerificationKycRepo,
    private val configurationRepository: ConfigurationRepository
) {

    suspend fun getVaccineDetailsData(vaccineId: String): VaccineCertDetailsDM {
        val data = verificationKycRepo.getVaccinationObjectData()
        if (data.exists()) {
            val vaccineObject = (data.get("vaccination") as Map<*, *>)
            return getRelatedVaccinData(vaccineObject, vaccineId,"",1)
        } else {
            throw Exception("Vaccination data does not exists!!")
        }
    }

    private fun getRelatedVaccinData(
        vaccineObject: Map<*, *>,
        vaccineId: String,
        vaccineLabel : String?,
        index:Int
    ): VaccineCertDetailsDM {
        val vaccineData = (vaccineObject.get(vaccineId) as? Map<*, *>)
        vaccineData?.let { vaccineData ->
            val age = vaccineData.get("age") as? String ?: ""
            val benificiaryId = vaccineData.get("benificiaryId") as? String ?: ""
            val ceritificateId = vaccineData.get("ceritificateId") as? String ?: ""
            val createdBy = vaccineData.get("createdBy") as? String ?: ""
            val fullPath = vaccineData.get("fullPath") as? String ?: ""
            val gender = vaccineData.get("gender") as? String ?: ""
            val label = vaccineData.get("label") as? String ?: ""
            val name = vaccineData.get("name") as? String ?: ""
            val pathOnFirebase = vaccineData.get("pathOnFirebase") as? String ?: ""
            val status = vaccineData.get("status") as? String ?: ""
            val updatedBy = vaccineData.get("updatedBy") as? String ?: ""
            val vaccineDate = vaccineData.get("vaccineDate") as? String ?: ""
            val vaccineName = vaccineData.get("vaccineName") as? String ?: ""
            val vaccinePlace = vaccineData.get("vaccinePlace") as? String ?: ""
            return VaccineCertDetailsDM(
                age = age,
                benificiaryId = benificiaryId,
                ceritificateId = ceritificateId,
                createdBy = createdBy,
                fullPath = fullPath,
                gender = gender,
                label = label,
                name = name,
                pathOnFirebase = pathOnFirebase,
                status = status,
                updatedBy = updatedBy,
                vaccineDate = vaccineDate,
                vaccineName = vaccineName,
                vaccinePlace = vaccinePlace,
                vaccineId = vaccineId,
                vaccineLabel = vaccineLabel,
                vaccineIndexLabel = "vaccine${index}"
            )
        }
        throw Exception("Vaccination data does not exists!!")
    }

    suspend fun getAllVaccinationDataList(): List<VaccineCertDetailsDM> {
        val allVaccine = ArrayList<VaccineCertDetailsDM>()
        val vaccineConfigData = configurationRepository.getVaccineConfigData()
        vaccineConfigData.list?.let {
            if (it.size > 0) {
                val data = verificationKycRepo.getVaccinationObjectData()
                if (data.exists()) {
                    val vaccineObject = (data.get("vaccination") as Map<*, *>)
                    for ((index,config) in it.withIndex()) {
                        config.id?.let { it1 ->
                            try {
                                allVaccine.add(getRelatedVaccinData(vaccineObject, it1, config.label,index+1))
                            } catch (e: Exception) {
                                allVaccine.add(VaccineCertDetailsDM(vaccineIndexLabel = "vaccine${index+1}",vaccineId = it1, vaccineLabel = config.label))
                            }
                        }
                    }
                }
            }
        }
        return allVaccine
    }

}