package com.gigforce.app.modules.preferences.location

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.preferences.SharedPreferenceViewModel
import com.gigforce.app.modules.preferences.prefdatamodel.PreferencesDataModel
import com.gigforce.app.modules.profile.models.AddressModel
import com.gigforce.app.modules.profile.models.ProfileData
import kotlinx.android.synthetic.main.permanent_address_edit_fragment.*

class PermanentAddressEditFragment : BaseFragment() {
    companion object {
        fun newInstance() = LocationFragment()
    }

    private lateinit var viewModel: SharedPreferenceViewModel
    private lateinit var preferenceDataModel: PreferencesDataModel
    private lateinit var profileDataModel: ProfileData
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.permanent_address_edit_fragment, inflater, container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SharedPreferenceViewModel::class.java)
        initializeViews()
        listener()
        observePreferenceData()
        observeProfileData()
    }

    private fun observePreferenceData() {
        viewModel.preferenceDataModel.observe(viewLifecycleOwner, Observer { preferenceData ->
            viewModel.setPreferenceDataModel(preferenceData)
            initializeViews()
        })

    }

    private fun observeProfileData() {
        viewModel.userProfileData.observe(viewLifecycleOwner, Observer { profileData ->
            viewModel.setProfileDataModel(profileData)
            initializeViews()
        })

    }

    private fun initializeViews() {
        preferenceDataModel = viewModel.getPreferenceDataModel()
        profileDataModel = viewModel.getProfileDataModel()
        verificationViewsInitialize()
        var currentAddress = viewModel.getCurrentAddress()
        var permanentAddress = viewModel.getPermanentAddress()
        populateAddress(permanentAddress!!)
    }

    private fun verificationViewsInitialize() {
        textView90.text = Html.fromHtml("To Update Permanent address , you have to upload new address proofs. <font color='#E02020'>Click Here</font>")
    }

    private fun populateAddress(address: AddressModel) {
        line1.setText(address.firstLine)
        line2.setText(address.secondLine)
        area.setText(address.area)
        location.setText(address.city)
        state.setText(address.state)
        pincode.setText(address.pincode)

    }

    private fun convertAddressToString(address: AddressModel?): String {
        Log.e("ADDRESS", address!!.firstLine)
        return if (address!!.isEmpty() || address == null)
            getString(R.string.add_address)
        else
            address.firstLine + "," + address.secondLine + "," + address.city + "," + address.state + ". " + address.pincode
    }

    private fun listener() {

        cancel_button.setOnClickListener {
            showToastLong("Cancel", 2)
            activity?.onBackPressed()
        }

        button2.setOnClickListener {
            showToastLong("Saving", 2)
            var editedAddress = AddressModel(
                line1.text.toString(),
                line2.text.toString(),
                area.text.toString(),
                location.text.toString(),
                state.text.toString(),
                pincode.text.toString()
            )
            Log.e("EDIT CURRENT", convertAddressToString(editedAddress))
            viewModel.setPermanentAddress(editedAddress)
            activity?.onBackPressed()
        }
        imageView10.setOnClickListener { activity?.onBackPressed() }
    }
}