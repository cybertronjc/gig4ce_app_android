package com.gigforce.app.modules.verification

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.validation.Regexes
import com.gigforce.app.modules.verification.models.Address
import kotlinx.android.synthetic.main.layout_verification.view.*
import java.util.regex.Matcher


class Verification: BaseFragment() {
    companion object {
        fun newInstance() = Verification()
    }

    var layout: View? = null
    lateinit var viewModel: VerificationViewModel
    var updates: ArrayList<Address> = ArrayList()
    lateinit var address1:String;
    lateinit var address2:String;
    private lateinit var city:String;
    lateinit var state:String;
    private lateinit var pincode:String;

    lateinit var match: Matcher;

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        layout = inflateView(R.layout.layout_verification, inflater,container);
        //requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        layout?.pbAddress?.setProgress(3,true)
        return layout
    }

    private fun observeVerficationData() {
        viewModel.veriData.observe(this, Observer { verification ->
            //if (verification!!.address!!.isNotEmpty()) {
            var lastSavedAddress = verification.Contact.get(verification.Contact.size-1)
            layout?.add_veri_address_line1?.setText(lastSavedAddress?.address)
            layout?.add_veri_address_line2?.setText(lastSavedAddress?.address)
            layout?.add_veri_address_state?.setText(lastSavedAddress?.state)
            layout?.add_veri_address_city?.setText(lastSavedAddress?.city)
            layout?.add_veri_address_pin?.setText(lastSavedAddress?.pincode)
            //}
        })
    }

    private fun validateFields(address1:String, address2:String, city:String, state:String, pincode:String):Boolean {
        //TODO Instead of toast msg we can put text msg on top of missing edit text or turn the edit text box to red!
        if (address1.isEmpty())
        {
            showToast("Please enter address1")
            layout?.add_veri_address_line1?.highlightColor = resources.getColor(R.color.colorAccent)
            return false
        }
        else if (address2.isEmpty())
        {
            showToast("Please enter address2")
            return false
        }
        else{
            match = Regexes.ADDRESS.matcher("$address1 $address2");
            if(!match.matches()) {
                showToast("Please enter valid address1 and address2")
                Log.d("Verification: ", "$address1 $address2")
                return false
            }
        }
        if(city.isEmpty())
        {
            showToast("Please enter city")
            return false
        }
        else
        {
            match = Regexes.CITY_STATE.matcher(city);
            if(!match.matches()) {
                showToast("Please enter valid city")
                Log.d("Verification: ", city)
                return false
            }
        }

        if(state.isEmpty())
        {
            showToast("Please enter state")
            return false
        }
        else
        {
            match = Regexes.CITY_STATE.matcher(state);
            if(!match.matches()) {
                showToast("Please enter valid state")
                Log.d("Verification: ", state)
                return false
            }
        }

        if(pincode.isEmpty())
        {
            showToast("Please enter pincode")
            return false
        }
        else
        {
            match = Regexes.PINCODE.matcher(pincode);
            if(!match.matches()) {
                showToast("Please enter valid pincode")
                Log.d("Verification: ", pincode)
                return false
            }
        }

        return true;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

        observeVerficationData();

        layout?.button_veri_address_cancel?.setOnClickListener {
            // CHECK to reset or not?
            //resetLayout();
            navigate(R.id.homeScreenIcons);
        }

        layout?.button_veri_address_save?.setOnClickListener {
            /*
            if fields on empty - validate fields (check for email regex and phone regex)
            and toast the missing fields before proceeding
             */

            address1 = layout?.add_veri_address_line1?.text.toString();
            address2 = layout?.add_veri_address_line2?.text.toString();
            city = layout?.add_veri_address_city?.text.toString();
            state = layout?.add_veri_address_state?.text.toString();
            pincode = layout?.add_veri_address_pin?.text.toString();

            //var areValid = validateFields(address1, address2, city, state, pincode);
            if(TextUtils.isEmpty(address1) || TextUtils.isEmpty(address2) || TextUtils.isEmpty(city) || TextUtils.isEmpty(state) || TextUtils.isEmpty(pincode))
            //TODO Is this check needed?
            //if(!areValid)
            {
                    showToast("Please fill up all the missing fields");
            }
            else{
                addNewContact()
                saveNewContacts()
                resetLayout()
                navigate(R.id.aadhaarUpload)
                //findNavController().navigate(R.id.panUpload)
            }
        }
    }

    private fun addNewContact() {
        updates.add(
            Address(
                address = "$address1 $address2",
                city = city,
                state = state,
                pincode = pincode
            )
        )
    }

    private fun resetLayout() {
        layout?.add_veri_address_line1?.setText("")
        layout?.add_veri_address_line2?.setText("")
        layout?.add_veri_address_city?.setText("")
        layout?.add_veri_address_state?.setText("")
        layout?.add_veri_address_pin?.setText("")
    }

    private fun saveNewContacts() {
        viewModel.setVerificationContact(updates)
    }
}