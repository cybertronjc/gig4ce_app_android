package com.gigforce.app.modules.gigerVerfication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import kotlinx.android.synthetic.main.fragment_giger_verification.*
import kotlinx.android.synthetic.main.fragment_giger_verification_documents_submitted.*
import kotlinx.android.synthetic.main.fragment_giger_verification_item.view.*
import kotlinx.android.synthetic.main.fragment_giger_verification_main.*
import kotlinx.android.synthetic.main.fragment_giger_verification_main.view.*


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
        initViewModel()
    }

    private fun initView() {

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }
        verificationCompletedBtn.setOnClickListener {
            activity?.onBackPressed()
        }

        Glide.with(requireContext())
            .load(R.drawable.ic_video_round)
            .into(selfieVideoLayout.optionIconIV)

        selfieVideoLayout.optionTitleTV.text = getString(R.string.selfie_video)
        selfieVideoLayout.descTitleTV.text = getString(R.string.tap_to_upload)


        Glide.with(requireContext())
            .load(R.drawable.ic_dl)
            .into(panLayout.optionIconIV)

        panLayout.optionTitleTV.text = getString(R.string.pan_card)
        panLayout.descTitleTV.text = getString(R.string.tap_to_upload)

        Glide.with(requireContext())
            .load(R.drawable.ic_bank)
            .into(aadharLayout.optionIconIV)

        aadharLayout.optionTitleTV.text = getString(R.string.aadhar_card)
        aadharLayout.descTitleTV.text = getString(R.string.tap_to_upload)


        Glide.with(requireContext())
            .load(R.drawable.ic_dl)
            .into(drivingLayout.optionIconIV)

        drivingLayout.optionTitleTV.text = getString(R.string.driving_license)
        drivingLayout.descTitleTV.text = getString(R.string.tap_to_upload)


        Glide.with(requireContext())
            .load(R.drawable.ic_bank)
            .into(bankDetailsLayout.optionIconIV)

        bankDetailsLayout.optionTitleTV.text = getString(R.string.bank_details)
        bankDetailsLayout.descTitleTV.text = getString(R.string.tap_to_upload)
    }


    private fun setListeners() {
        verificationMainLayout.panLayout.setOnClickListener {
            navigate(R.id.addPanCardInfoFragment)
        }

        verificationMainLayout.drivingLayout.setOnClickListener {
            navigate(R.id.addDrivingLicenseInfoFragment)
        }

        verificationMainLayout.aadharLayout.setOnClickListener {
            navigate(R.id.addAadharCardInfoFragment)
        }

        verificationMainLayout.bankDetailsLayout.setOnClickListener {
            navigate(R.id.addBankDetailsInfoFragment)
        }

        verificationMainLayout.selfieVideoLayout.setOnClickListener {
            navigate(R.id.addSelfieVideoFragment)
        }
    }

    private fun initViewModel() {
        viewModel.gigerVerificationStatus
            .observe(viewLifecycleOwner, Observer {

                if (it.everyDocumentUploaded) {
                    verificationMainLayout.gone()
                    verificationDocSubmittedLayout.visible()

                } else {
                    verificationDocSubmittedLayout.gone()
                    verificationMainLayout.visible()

                    if (it.selfieVideoUploaded) {
                        selfieVideoLayout.descTitleTV.text = getString(R.string.uploaded)
                        selfieVideoLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.green,
                                null
                            )
                        )
                    } else {
                        selfieVideoLayout.descTitleTV.text = getString(R.string.tap_to_upload)
                        selfieVideoLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.battle_ship_grey,
                                null
                            )
                        )
                    }

                    if (it.panCardDetailsUploaded) {
                        panLayout.descTitleTV.text = getString(R.string.uploaded)
                        panLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.green,
                                null
                            )
                        )
                    } else {
                        panLayout.descTitleTV.text = getString(R.string.tap_to_upload)
                        panLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.battle_ship_grey,
                                null
                            )
                        )
                    }



                    if (it.bankDetailsUploaded) {
                        bankDetailsLayout.descTitleTV.text = getString(R.string.uploaded)
                        bankDetailsLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.green,
                                null
                            )
                        )
                    } else {
                        bankDetailsLayout.descTitleTV.text = getString(R.string.tap_to_upload)
                        bankDetailsLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.battle_ship_grey,
                                null
                            )
                        )
                    }



                    if (it.aadharCardDetailsUploaded) {
                        aadharLayout.descTitleTV.text = getString(R.string.uploaded)
                        aadharLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.green,
                                null
                            )
                        )
                    } else {
                        aadharLayout.descTitleTV.text = getString(R.string.tap_to_upload)
                        aadharLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.battle_ship_grey,
                                null
                            )
                        )
                    }



                    if (it.dlCardDetailsUploaded) {
                        drivingLayout.descTitleTV.text = getString(R.string.uploaded)
                        drivingLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.green,
                                null
                            )
                        )
                    } else {
                        drivingLayout.descTitleTV.text = getString(R.string.tap_to_upload)
                        drivingLayout.descTitleTV.setTextColor(
                            ResourcesCompat.getColor(
                                resources,
                                R.color.battle_ship_grey,
                                null
                            )
                        )
                    }

                }
            })

        viewModel.startListeningForGigerVerificationStatusChanges()
    }
}