package com.gigforce.verification.mainverification.vaccine.views

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewbinding.ViewBinding
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.utils.Lce
import com.gigforce.verification.R
import com.gigforce.verification.databinding.FragmentCovidVaccinationCertificateBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CovidVaccinationCertificateFragment : Fragment() {

    lateinit var viewBinding: FragmentCovidVaccinationCertificateBinding
    val viewModel : VaccineViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = FragmentCovidVaccinationCertificateBinding.inflate(inflater,container,false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.getVaccineDetailsData("vaccine1")
        observer()
    }

    private fun observer() {
        viewModel.vaccineCertDetailsDM.observe(viewLifecycleOwner, Observer {
            when(it) {
                is Lce.Content -> viewBinding.vaccinationDoseDetails.setData(it.content.name?:"", it.content.age?:"",it.content.gender?:"",it.content.ceritificateId?:"", it.content.benificiaryId?:"",it.content.vaccineName?:"",it.content.vaccineDate?:"",it.content.status?:"",it.content.vaccinePlace?:"")
                is Lce.Error -> {showToast(it.error)}
                else -> {}
            }
        })
    }

}