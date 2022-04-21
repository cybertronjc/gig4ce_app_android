package com.gigforce.verification.mainverification.vaccine.models

import com.gigforce.core.SimpleDVM
import com.gigforce.core.datamodels.CommonViewTypes

data class VaccineConfigDM(val id : String?="", val label :String?=""): SimpleDVM(CommonViewTypes.VACCINE_DOSE_CARD)