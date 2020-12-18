package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.currentAddress

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import com.afollestad.materialdialogs.utils.MDUtil.textChanged
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.ambassador_user_enrollment.EnrollmentConstants
import com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.user_details.UserDetailsViewModel
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lse
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_user_current_address.*

class AddCurrentAddressFragment : BaseFragment() {

    private val viewModel: UserDetailsViewModel by viewModels()
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_user_current_address, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getDataFromIntents(arguments, savedInstanceState)
        initListeners()
        initViewModel()
    }

    private fun getDataFromIntents(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
        }

        savedInstanceState?.let {
            userId = it.getString(EnrollmentConstants.INTENT_EXTRA_USER_ID) ?: return@let
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(EnrollmentConstants.INTENT_EXTRA_USER_ID, userId)
    }

    private fun initListeners() {
        pin_code_et.textChanged {
            pin_code_okay_iv.isVisible = it.length == 6 && it.toString().toInt() > 10_00_00
        }

        arround_current_add_seekbar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val value =
                    (progress * (seekBar.getWidth() - 2 * seekBar.getThumbOffset())) / seekBar.getMax()
                seekbardependent.text = progress.toString() + " " + getString(R.string.km)
                seekbardependent.setX(seekBar.getX() + value + seekBar.getThumbOffset() / 2)
                //textView.setY(100); just added a value set this properly using screen with height aspect ratio , if you do not set it by default it will be there below seek bar
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        submitBtn.setOnClickListener {
            validateDataAndSubmit()
        }
    }

    private fun validateDataAndSubmit() {
        if (pin_code_et.text.length != 6 && pin_code_et.text.toString().toInt() > 10_00_00) {
            showAlertDialog("Invalid pincode", "Provide a valid Pin Code")
            return
        }

        if (address_line_1_et.text.isBlank()) {
            showAlertDialog("Provide Address Line 1", "Please provide address line 1")
            return
        }

        if (address_line_2_et.text.isBlank()) {
            showAlertDialog("Provide Address Line 2", "Please provide address line 2")
            return
        }

        if (state_spinner.selectedItemPosition == 0) {
            showAlertDialog("Provide State", "Please select state")
            return
        }

        if (city_et.text.isBlank()) {
            showAlertDialog("Provide City", "Please provide city name")
            return
        }

        if (ready_to_change_location_chipgroup.checkedChipId == -1) {
            showAlertDialog("", "Select if you want to change your location")
            return
        }

        viewModel.updateUserCurrentAddressDetails(
            uid = userId,
            pinCode = pin_code_et.text.toString(),
            addressLine1 = address_line_1_et.text.toString(),
            addressLine2 = address_line_2_et.text.toString(),
            state = state_spinner.selectedItem.toString(),
            city = city_et.text.toString(),
            preferredDistanceInKm = arround_current_add_seekbar.progress,
            readyToChangeLocationForWork = ready_to_change_location_chipgroup.checkedChipId == R.id.chip_location_change_yes
        )
    }

    private fun initViewModel() {
        viewModel.submitUserDetailsState
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lse.Loading -> {
                       // UtilMethods.showLoading(requireContext())
                    }
                    Lse.Success -> {
                       // UtilMethods.hideLoading()
                        showToast("User Current Address Details submitted")

                        navigate(
                            R.id.addUserPanCardInfoFragment, bundleOf(
                                EnrollmentConstants.INTENT_EXTRA_USER_ID to userId
                            )
                        )
                    }
                    is Lse.Error -> {
                      //  UtilMethods.hideLoading()
                        showAlertDialog("Could not submit address info", it.error)
                    }
                }
            })
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }
}