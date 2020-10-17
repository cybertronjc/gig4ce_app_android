package com.gigforce.app.modules.gigerVerfication

import android.content.Intent
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
import com.gigforce.app.core.visible
import com.gigforce.app.utils.DocViewerActivity
import com.gigforce.app.utils.PushDownAnim
import com.gigforce.app.utils.StringConstants
import kotlinx.android.synthetic.main.fragment_giger_verification.*
import kotlinx.android.synthetic.main.fragment_giger_verification_item.view.*
import kotlinx.android.synthetic.main.fragment_giger_verification_main.*
import kotlinx.android.synthetic.main.fragment_giger_verification_main.view.*


class GigerVerificationFragment : BaseFragment() {

    private val viewModel: GigVerificationViewModel by viewModels()
    private var gigerVerificationStatus: GigerVerificationStatus? = null

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
        checkForContract()
    }

    private fun checkForContract() {
        viewModel.gigerContractStatus.observe(viewLifecycleOwner, Observer { url ->
            run {
                ll_contracts.visible()
                if (url != null) {
                    tv_contract_status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_check,
                        0,
                        0,
                        0
                    )
                    tv_contract_status.setTextColor(resources.getColor(R.color.green_dc3ab105))
                    tv_contract_status.setBackgroundResource(R.drawable.bg_capsule_53ba25)
                    tv_contract_status.text = getString(R.string.signed)
                    PushDownAnim.setPushDownAnimTo(ll_contracts)
                        .setOnClickListener(View.OnClickListener {
                            val docIntent = Intent(
                                requireContext(),
                                DocViewerActivity::class.java
                            )
                            docIntent.putExtra(
                                StringConstants.DOC_URL.value,
                                url
                            )
                            startActivity(docIntent)
                        })


                } else {
                    tv_contract_status.setCompoundDrawablesRelativeWithIntrinsicBounds(
                        R.drawable.ic_time_fa6400,
                        0,
                        0,
                        0
                    )
                    tv_contract_status.setTextColor(resources.getColor(R.color.fa6400))
                    tv_contract_status.setBackgroundResource(R.drawable.bg_capsule_border_fa6400)
                    tv_contract_status.text = getString(R.string.unsigned)
                }
            }

        })
        viewModel.checkForSignedContract()
    }

    private fun initView() {

        toolbar.setNavigationOnClickListener {
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
                verificationMainLayout.visible()
                this.gigerVerificationStatus = it

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

                if (it.panCardDetails?.userHasPanCard != null && it.panCardDetails.userHasPanCard) {
                    panLayout.descTitleTV.text = it.panCardDetails.verifiedString

                    panLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            it.getColorCodeForStatus(it.panCardDetails.state),
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



                if (it.bankUploadDetailsDataModel?.userHasPassBook != null && it.bankUploadDetailsDataModel.userHasPassBook) {
                    bankDetailsLayout.descTitleTV.text =
                        it.bankUploadDetailsDataModel.verifiedString
                    bankDetailsLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            it.getColorCodeForStatus(
                                it.bankUploadDetailsDataModel.state
                            ),
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



                if (it.aadharCardDataModel?.userHasAadharCard != null && it.aadharCardDataModel.userHasAadharCard) {
                    aadharLayout.descTitleTV.text = it.aadharCardDataModel.verifiedString
                    aadharLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            it.getColorCodeForStatus(it.aadharCardDataModel.state),
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



                if (it.drivingLicenseDataModel?.userHasDL != null && it.drivingLicenseDataModel.userHasDL) {
                    drivingLayout.descTitleTV.text = it.drivingLicenseDataModel.verifiedString
                    drivingLayout.descTitleTV.setTextColor(
                        ResourcesCompat.getColor(
                            resources,
                            it.getColorCodeForStatus(it.drivingLicenseDataModel.state),
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
            })

        viewModel.startListeningForGigerVerificationStatusChanges()
    }
}