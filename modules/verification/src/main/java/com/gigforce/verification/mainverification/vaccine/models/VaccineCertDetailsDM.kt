package com.gigforce.verification.mainverification.vaccine.models

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class VaccineCertDetailsDM(val age : String?="",val benificiaryId : String?="",val ceritificateId:String?="", val createdBy : String ?= null, val fullPath : String?=null, val gender : String?=null, val label : String?=null, val name : String?=null, val pathOnFirebase : String ?= null, val status :String?=null, val updatedBy : String?=null, val vaccineDate: String?=null, val vaccineName : String?=null, val vaccinePlace : String?=null, val vaccineId : String?=null, val vaccineLabel:String?=null, val vaccineIndexLabel : String? = null):SimpleDVM(CommonViewTypes.VACCINE_DOSE_STATUS)
