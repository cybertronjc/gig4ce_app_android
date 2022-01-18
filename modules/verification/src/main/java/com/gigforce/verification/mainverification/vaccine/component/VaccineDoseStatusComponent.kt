package com.gigforce.verification.mainverification.vaccine.component

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.os.bundleOf
import com.gigforce.core.IViewHolder
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.mainverification.vaccine.models.VaccineCertDetailsDM
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.vaccine_dose_status.view.*
import javax.inject.Inject

@AndroidEntryPoint
class VaccineDoseStatusComponent(context: Context, attrs: AttributeSet?) :
    FrameLayout(context, attrs),
    IViewHolder  {

    init {
        this.layoutParams =
            LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        LayoutInflater.from(context).inflate(R.layout.vaccine_dose_status, this, true)
    }

    @Inject
    lateinit var navigation : INavigation

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

    override fun bind(data: Any?) {
        if(data is VaccineCertDetailsDM){
            setData(data.name?:"",data.ceritificateId?:"",data.vaccineName?:"",data.vaccineDate?:"")
            view_more.setOnClickListener{
                if(data.status.isNullOrBlank()){
                    navigation.navigateTo("verification/chooseYourVaccineFragment")
                }
                else{
                    navigation.navigateTo("verification/CovidVaccinationCertificateFragment", bundleOf("vaccineId" to data.vaccineId,"download_certificate" to true))
                }
            }
        }

    }


}