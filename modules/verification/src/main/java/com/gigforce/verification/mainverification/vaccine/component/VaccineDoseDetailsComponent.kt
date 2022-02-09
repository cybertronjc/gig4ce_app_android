package com.gigforce.verification.mainverification.vaccine.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import com.gigforce.verification.R
import kotlinx.android.synthetic.main.vaccine_dose_detail.view.*

class VaccineDoseDetailsComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs) {

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.vaccine_dose_detail, this, true)
    }

    fun setData(
        name: String,
        age: String,
        gender: String,
        certificateId: String,
        beneficiaryId: String,
        vaccinationName: String,
        dateOfDose: String,
        vaccinationStatus: String,
        vaccinationAt: String
    ) {
        name_tv.text = name
        age_tv.text = age
        gender_tv.text = gender
        certificateid_tv.text = certificateId
        baneficiaryid_tv.text = beneficiaryId
        vaccinename_tv.text = vaccinationName
        dateofdose_tv.text = dateOfDose
        vaccinationstatus_tv.text = vaccinationStatus
        vaccinationat_tv.text = vaccinationAt
    }


}