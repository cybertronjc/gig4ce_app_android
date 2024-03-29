package com.gigforce.user_preferences.location

import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.core.datamodels.profile.AddressModel
import com.gigforce.core.datamodels.profile.ProfileData
import com.gigforce.user_preferences.R
import com.gigforce.common_ui.viewmodels.userpreferences.SharedPreferenceViewModel
import com.gigforce.core.datamodels.user_preferences.PreferencesDataModel
import kotlinx.android.synthetic.main.permanent_address_edit_fragment.*

class PermanentAddressEditFragment : Fragment() {
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
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        return inflater.inflate(R.layout.permanent_address_edit_fragment, container, false)
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
        textView90.text = Html.fromHtml(getString(R.string.upload_new_address_pref))
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
            getString(R.string.add_address_pref)
        else
            address.firstLine + "," + address.secondLine + "," + address.city + "," + address.state + ". " + address.pincode
    }

    private fun listener() {

        cancel_button.setOnClickListener {
//            showToastLong("Cancel", 2)
            activity?.onBackPressed()
        }

        button2.setOnClickListener {
//            showToastLong("Saving", 2)
            if(validate()) {
                var editedAddress = AddressModel(
                    line1.text.toString(),
                    line2.text.toString(),
                    area.text.toString(),
                    location.text.toString(),
                    state.text.toString(),
                    pincode.text.toString()
                )
                viewModel.setPermanentAddress(editedAddress)
                activity?.onBackPressed()
            }
        }
        back_arrow_iv.setOnClickListener { activity?.onBackPressed() }

    }
    fun addressIsValid(view:EditText):Boolean{
        if(view.text.toString().trim().length<3){
            view.setError(getString(R.string.more_details_required_pref))
            return false
        }
        return true
    }
    private fun validate(): Boolean {
        if(!addressIsValid(line1))
            return false
        if(!addressIsValid(line2))
            return false
        if(!addressIsValid(area))
            return false
        if(!addressIsValid(location))
            return false
        if(!addressIsValid(state))
            return false
        if(pincode.text.toString().length<6){
            pincode.setError(getString(R.string.pincode_incorrect_pref))
            return false
        }
        return true
    }
}