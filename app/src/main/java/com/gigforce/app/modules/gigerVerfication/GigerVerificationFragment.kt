package com.gigforce.app.modules.gigerVerfication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_giger_verification.*
import kotlinx.android.synthetic.main.fragment_giger_verification_item.view.*


class GigerVerificationFragment : BaseFragment() {

    private val viewModel: GigVerificationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_giger_verification, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        setListeners()
    }

    private fun initView() {
        Glide.with(requireContext())
            .load(R.drawable.ic_video_round)
            .into(selfieVideoLayout.optionIconIV)

        selfieVideoLayout.optionTitleTV.text = getString(R.string.selfie_video)
        selfieVideoLayout.descTitleTV.text = getString(R.string.tap_to_upload)


        Glide.with(requireContext())
            .load(R.drawable.ic_video_round)
            .into(panLayout.optionIconIV)

        panLayout.optionTitleTV.text = getString(R.string.pan_card)
        panLayout.descTitleTV.text = getString(R.string.tap_to_upload)

        Glide.with(requireContext())
            .load(R.drawable.ic_video_round)
            .into(aadharLayout.optionIconIV)

        aadharLayout.optionTitleTV.text = getString(R.string.aadhar_card)
        aadharLayout.descTitleTV.text = getString(R.string.tap_to_upload)


        Glide.with(requireContext())
            .load(R.drawable.ic_video_round)
            .into(drivingLayout.optionIconIV)

        drivingLayout.optionTitleTV.text = getString(R.string.driving_license)
        drivingLayout.descTitleTV.text = getString(R.string.tap_to_upload)


        Glide.with(requireContext())
            .load(R.drawable.ic_video_round)
            .into(bankDetailsLayout.optionIconIV)

        bankDetailsLayout.optionTitleTV.text = getString(R.string.bank_details)
        bankDetailsLayout.descTitleTV.text = getString(R.string.tap_to_upload)
    }


    private fun setListeners() {
        panLayout.setOnClickListener {
            findNavController().navigate(R.id.addPanCardInfoFragment)
        }

        drivingLayout.setOnClickListener {
            findNavController().navigate(R.id.addDrivingLicenseInfoFragment)
        }

        aadharLayout.setOnClickListener {
            findNavController().navigate(R.id.addAadharCardInfoFragment)
        }
    }
}