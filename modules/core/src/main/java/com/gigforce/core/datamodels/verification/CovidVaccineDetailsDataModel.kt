package com.gigforce.core.datamodels.verification

data class CovidVaccineDetailsDataModel(val list : CovidVaccineDataModel? = null, val status : String?=null, val statusString : String?=null)

data class CovidVaccineDataModel(val label : String?=null, var status : String?=null)