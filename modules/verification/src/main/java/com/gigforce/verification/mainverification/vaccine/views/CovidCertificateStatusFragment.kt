package com.gigforce.verification.mainverification.vaccine.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.core.utils.Lce
import com.gigforce.verification.R
import com.gigforce.verification.mainverification.vaccine.VaccineViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_covid_certificate_status.*

@AndroidEntryPoint
class CovidCertificateStatusFragment : Fragment() {

    val viewModel: VaccineViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_covid_certificate_status, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeAll()
        observers()
    }

    private fun initializeAll() {
        viewModel.getAllVaccineDetailData()
    }

    private fun observers() {

        viewModel.allVaccineLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                }
                is Lce.Error -> {
                }
                is Lce.Content -> {
                    all_vaccineStatus.collection = it.content
                }
            }
        })

    }

}