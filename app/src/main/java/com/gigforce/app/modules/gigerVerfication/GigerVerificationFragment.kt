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
import com.gigforce.app.core.visible
import kotlinx.android.synthetic.main.fragment_giger_verification.*
import kotlinx.android.synthetic.main.fragment_giger_verification_documents_submitted.*
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

            if(gigerVerificationStatus?.panCardDetails == null
                || gigerVerificationStatus?.panCardDetails?.state == GigerVerificationStatus.STATUS_VERIFICATION_FAILED
                || gigerVerificationStatus?.panCardDetails?.userHasPanCard!!.not()) {
                navigate(R.id.addPanCardInfoFragment)
            } else {

            }
        }

        verificationMainLayout.drivingLayout.setOnClickListener {
            if(gigerVerificationStatus?.drivingLicenseDataModel == null
                || gigerVerificationStatus?.drivingLicenseDataModel?.state == GigerVerificationStatus.STATUS_VERIFICATION_FAILED
                || gigerVerificationStatus?.drivingLicenseDataModel?.userHasDL!!.not()) {
                navigate(R.id.addDrivingLicenseInfoFragment)
            } else {

            }

        }

        verificationMainLayout.aadharLayout.setOnClickListener {


            if(gigerVerificationStatus?.aadharCardDataModel == null
                || gigerVerificationStatus?.aadharCardDataModel?.state == GigerVerificationStatus.STATUS_VERIFICATION_FAILED
                || gigerVerificationStatus?.aadharCardDataModel?.userHasAadharCard!!.not()) {
                navigate(R.id.addAadharCardInfoFragment)
            } else {

            }

        }

        verificationMainLayout.bankDetailsLayout.setOnClickListener {

            if(gigerVerificationStatus?.bankUploadDetailsDataModel == null
                || gigerVerificationStatus?.bankUploadDetailsDataModel?.state == GigerVerificationStatus.STATUS_VERIFICATION_FAILED
                || gigerVerificationStatus?.bankUploadDetailsDataModel?.userHasPassBook!!.not()) {
                navigate(R.id.addBankDetailsInfoFragment)
            } else {

            }
        }

        verificationMainLayout.selfieVideoLayout.setOnClickListener {

            if(gigerVerificationStatus?.selfieVideoDataModel?.videoPath == null) {
                navigate(R.id.addSelfieVideoFragment)
            }
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