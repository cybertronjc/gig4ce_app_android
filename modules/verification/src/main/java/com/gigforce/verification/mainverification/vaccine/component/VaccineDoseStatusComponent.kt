package com.gigforce.verification.mainverification.vaccine.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.verification.R
import kotlinx.android.synthetic.main.vaccine_dose_status.view.*

class VaccineDoseStatusComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.vaccine_dose_status, this, true)
    }

    fun setData(
        name: String,
        certificateId: String,
        vaccinationName: String,
        dateOfDose: String
    ) {
        name_label.text = name
        certificateid_tv.text = certificateId
        vaccinename_tv.text = vaccinationName
        dateofdose_tv.text = dateOfDose
    }


}