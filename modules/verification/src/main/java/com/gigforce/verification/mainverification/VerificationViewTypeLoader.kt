package com.gigforce.verification.mainverification

import android.content.Context
import android.view.View
import com.gigforce.core.IViewTypeLoader
import com.gigforce.core.datamodels.CommonViewTypes
import com.gigforce.verification.mainverification.vaccine.component.VaccineDoseStatusComponent

object VerificationViewTypeLoader : IViewTypeLoader {
    override fun getView(context: Context, viewType: Int): View? {
        return when(viewType){
            CommonViewTypes.VACCINE_DOSE_STATUS -> return VaccineDoseStatusComponent(context,null)
            else -> null
        }
    }
}