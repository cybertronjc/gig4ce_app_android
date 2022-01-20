package com.gigforce.verification.mainverification.vaccine.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.verification.databinding.FragmentCovidVaccinationCertificateBinding
import com.gigforce.verification.mainverification.vaccine.VaccineViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class CovidVaccinationCertificateFragment : Fragment() {

    lateinit var viewBinding: FragmentCovidVaccinationCertificateBinding
    val viewModel: VaccineViewModel by viewModels()
    var vaccineId: String? = null
    var downloadOption = false

    @Inject
    lateinit var navigation: INavigation
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        viewBinding = FragmentCovidVaccinationCertificateBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData()
        vaccineId?.let {
            viewModel.getVaccineDetailsData(it)
        }
        initializeViews()
        observer()
        listener()
    }

    private fun initializeViews() {
        if (downloadOption) {
            viewBinding.confirmBnBs.gone()
            viewBinding.changeDocButton.gone()
            viewBinding.docDownload.visible()
        } else {
            viewBinding.confirmBnBs.visible()
            viewBinding.changeDocButton.visible()
            viewBinding.docDownload.gone()
        }
    }

    private fun listener() {
        viewBinding.confirmBnBs.setOnClickListener {
            vaccineId?.let {
                viewModel.confirmVaccineData(it)
            }
        }

        viewBinding.changeDocButton.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun getIntentData() {
        arguments?.let {
            vaccineId = it.getString("vaccineId")
            downloadOption = it.getBoolean("download_certificate")


        }
    }

    private fun observer() {
        viewModel.vaccineCertDetailsDM.observe(viewLifecycleOwner, Observer {
            when (it) {
                is Lce.Content -> viewBinding.vaccinationDoseDetails.setData(
                    it.content.name ?: "",
                    it.content.age ?: "",
                    it.content.gender ?: "",
                    it.content.ceritificateId ?: "",
                    it.content.benificiaryId ?: "",
                    it.content.vaccineName ?: "",
                    it.content.vaccineDate ?: "",
                    it.content.status ?: "",
                    it.content.vaccinePlace ?: ""
                )
                is Lce.Error -> {
                    showToast(it.error)
                }
                else -> {
                }
            }
        })

        viewModel.confirmVaccineDetailLiveData.observe(viewLifecycleOwner, Observer {
            when (it) {
                Lce.Loading -> {
                }
                is Lce.Content -> {
                    navigation.popBackStack()
                    navigation.navigateTo("verification/CovidCertificateStatusFragment")
                }
                is Lce.Error -> {
                    showToast(it.error)
                }

            }
        })
    }

}