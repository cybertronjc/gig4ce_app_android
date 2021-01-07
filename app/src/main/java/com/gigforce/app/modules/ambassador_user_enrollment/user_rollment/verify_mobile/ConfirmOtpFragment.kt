package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lce
import com.gigforce.app.utils.LocationUpdates
import com.gigforce.app.utils.PermissionUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_confirm_otp.*

class ConfirmOtpFragment : BaseFragment(), LocationUpdates.LocationUpdateCallbacks {
    private var location: Location? = null
    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
    }
    private val viewModel: VerifyUserMobileViewModel by viewModels()

    private lateinit var verificationToken: String
    private lateinit var mobileNo: String

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_confirm_otp, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }

        savedInstanceState?.let {
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_MOBILE_NO, mobileNo)
        outState.putString(INTENT_EXTRA_OTP_TOKEN, verificationToken)
    }

    private fun initListeners() {
        we_will_send_otp_label.text = "We sent it to the number +91 - $mobileNo"

        submitBtn.setOnClickListener {
            validateDataAndsubmit()
        }

        ic_back_iv.setOnClickListener {
            activity?.onBackPressed()
        }
    }

    private fun validateDataAndsubmit() {
        if (txt_otp.text?.length != 6) {
            showAlertDialog("", "Enter a valid otp")
            return
        }
        if (location == null) {
            showAlertDialog("", getString(R.string.location_error))
        }

        viewModel.checkOtpAndCreateProfile(
                verificationToken,
                txt_otp.text.toString(),
                mobileNo,
                location!!
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("Okay") { _, _ -> }
                .show()
    }

    private fun initViewModel() {
        viewModel.createProfile
                .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                    when (it) {
                        Lce.Loading -> {
                            UtilMethods.showLoading(requireContext())
                        }
                        is Lce.Content -> {
                            UtilMethods.hideLoading()
                            showToast("Otp Confirmed, Profile Created")
                            navigate(
                                    R.id.addUserDetailsFragment, bundleOf(
                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to it.content.uid,
                                    EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to it.content.phoneNumber
                            )
                            )
                        }
                        is Lce.Error -> {
                            UtilMethods.hideLoading()
                            showAlertDialog("", it.error)
                        }
                    }
                })
    }

    override fun onDestroy() {
        super.onDestroy()
        locationUpdates.stopLocationUpdates(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        locationUpdates.startUpdates(requireActivity() as AppCompatActivity)
        locationUpdates.setLocationUpdateCallbacks(this)
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String?>,
            grantResults: IntArray
    ) {
        when (requestCode) {

            LocationUpdates.REQUEST_PERMISSIONS_REQUEST_CODE -> if (PermissionUtils.permissionsGrantedCheck(
                            grantResults
                    )
            ) {
                locationUpdates!!.startUpdates(requireActivity() as AppCompatActivity)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {

            LocationUpdates.REQUEST_CHECK_SETTINGS -> if (resultCode == Activity.RESULT_OK) locationUpdates.startUpdates(
                    requireActivity() as AppCompatActivity
            )

        }
    }

    companion object {
        const val INTENT_EXTRA_MOBILE_NO = "mobileNo"
        const val INTENT_EXTRA_OTP_TOKEN = "otp_token"
    }

    override fun locationReceiver(location: Location?) {
        this.location = location
    }

    override fun lastLocationReceiver(location: Location?) {
    }
}