package com.gigforce.ambassador.user_rollment.verify_mobile

import android.app.Activity
import android.content.Intent
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import com.gigforce.ambassador.EnrollmentConstants
import com.gigforce.ambassador.R
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.LocationUpdates
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.invisible
import com.gigforce.core.extensions.visible
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.Lce
import com.gigforce.core.utils.PermissionUtils
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_ambsd_confirm_otp.*
import javax.inject.Inject

@AndroidEntryPoint
class ConfirmOtpFragment : Fragment(), LocationUpdates.LocationUpdateCallbacks {
    private var location: Location? = null
    private val locationUpdates: LocationUpdates by lazy {
        LocationUpdates()
    }
    private val viewModel: VerifyUserMobileViewModel by viewModels()

    private lateinit var verificationToken: String
    private lateinit var mobileNo: String

    private var userId: String? = null
    private var mode = EnrollmentConstants.MODE_ADD

    @Inject
    lateinit var navigation: INavigation

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_ambsd_confirm_otp, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initView()
        initListeners()
        initViewModel()
    }

    private fun initView() {
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {

            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }

        savedInstanceState?.let {

            mode = it.getInt(EnrollmentConstants.INTENT_EXTRA_MODE)
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID)
            mobileNo = it.getString(INTENT_EXTRA_MOBILE_NO) ?: return@let
            verificationToken = it.getString(INTENT_EXTRA_OTP_TOKEN) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_MOBILE_NO, mobileNo)
        outState.putString(INTENT_EXTRA_OTP_TOKEN, verificationToken)
        outState.putInt(EnrollmentConstants.INTENT_EXTRA_MODE, mode)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
    }

    private fun initListeners() {
        we_will_send_otp_label.text = getString(R.string.sent_otp_to_the_number) + mobileNo

        submitBtn.setOnClickListener {
            validateDataAndSubmit()
        }

        toolbar_layout.hideActionMenu()
        toolbar_layout.setBackButtonListener(View.OnClickListener {
            activity?.onBackPressed()
        })
    }

    private fun validateDataAndSubmit() {
        if (txt_otp.text?.length != 6) {
            showAlertDialog("", getString(R.string.enter_valid_otp))
            return
        }

//        if (location == null) {
//            showAlertDialog("", getString(R.string.location_error))
//        }

        viewModel.checkOtpAndCreateProfile(
            userId = userId,
            mode = mode,
            token = verificationToken,
            otp = txt_otp.text.toString(),
            mobile = mobileNo,
            latitude = location?.latitude ?: 0.0,
            longitude = location?.longitude ?: 0.0,
            fullAddress = processLocationAndUpdateUserDetails(location)
        )
    }

    fun processLocationAndUpdateUserDetails(location: Location?): String {

        if (location == null)
            return ""

        val latitude: Double = location.latitude
        val longitude: Double = location.longitude

        var locationAddress = ""
        try {
            val geocoder = Geocoder(requireContext())
            val addressArr = geocoder.getFromLocation(latitude, longitude, 1)
            locationAddress = addressArr?.get(0)?.getAddressLine(0) ?: ""
        } catch (e: Exception) {

        }

        return locationAddress
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.okay)) { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        viewModel.createProfile
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {
                when (it) {
                    Lce.Loading -> {
                        submitBtn.invisible()
                        confirming_otp_pb.visible()
                    }
                    is Lce.Content -> {
                        showToast(getString(R.string.otp_confimed))

                        if (mode == EnrollmentConstants.MODE_EDIT) {
                            navigation.navigateTo(
                                "userinfo/addUserDetailsFragment", bundleOf(
                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
                                    EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to mobileNo,
                                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
                                )
                            )
//                            navigate(
//                                R.id.addUserDetailsFragment, bundleOf(
//                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to userId,
//                                    EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to mobileNo,
//                                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
//                                )
//                            )
                        } else {
                            navigation.navigateTo(
                                "userinfo/addUserDetailsFragment", bundleOf(
                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to it.content.uid,
                                    EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to it.content.phoneNumber,
                                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
                                )
                            )
//                            navigate(
//                                R.id.addUserDetailsFragment, bundleOf(
//                                    EnrollmentConstants.INTENT_EXTRA_USER_ID to it.content.uid,
//                                    EnrollmentConstants.INTENT_EXTRA_PHONE_NUMBER to it.content.phoneNumber,
//                                    EnrollmentConstants.INTENT_EXTRA_MODE to mode
//                                )
//                            )
                        }
                    }
                    is Lce.Error -> {
                        confirming_otp_pb.gone()
                        submitBtn.visible()

                        showAlertDialog("", it.error)
                    }
                }
            })

    }


    override fun onPause() {
        super.onPause()
        locationUpdates.stopLocationUpdates(activity)
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
                locationUpdates.startUpdates(requireActivity() as AppCompatActivity)
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