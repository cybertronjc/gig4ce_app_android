package com.gigforce.app.modules.verification

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.auth.ui.main.Login
import com.gigforce.app.modules.verification.models.Address
import kotlinx.android.synthetic.main.layout_verification.view.*

class Verification: Fragment() {
    companion object {
        fun newInstance() = Verification()
    }

    lateinit var layout: View
    lateinit var viewModel: VerificationViewModel
    var updates: ArrayList<Address> = ArrayList()
    lateinit var address1:String;
    lateinit var address2:String;
    private lateinit var city:String;
    lateinit var state:String;
    private lateinit var pincode:String;

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        layout = inflater.inflate(R.layout.layout_verification, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        layout.pbAddress.setProgress(1,true)
        return layout
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() { // Handle the back button event
                onBackPressed()
            }
        }
    fun onBackPressed() {
            findNavController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

//        viewModel.veriData.observe(this, Observer { contact ->
//            var contactString = ""
//            //for (contact in contacts!!) {
//                contactString += "address: "+contact.address +"\n"
//                contactString += "phone: " + contact.phone + "\n"
//                contactString += "email: " + contact.email + "\n\n"
//            //}
//            //layout.add_veri_contact_address.text = contact.address
//        })

        layout.button_veri_address_cancel.setOnClickListener {
            resetLayout();
            findNavController().navigate(R.id.homeScreenIcons);
        }

        layout.button_veri_address_save.setOnClickListener {
                /*
                if fields on empty - validate fields (check for email regex and phone regex)
                and toast the missing fields before proceeding
                 */

            address1 = layout.add_veri_address_line1.text.toString();
            address2 = layout.add_veri_address_line2.text.toString();
            city = layout.add_veri_address_city.text.toString();
            state = layout.add_veri_address_state.text.toString();
            pincode = layout.add_veri_address_pin.text.toString();

            if(TextUtils.isEmpty(address1) || TextUtils.isEmpty(address2) || TextUtils.isEmpty(city) || TextUtils.isEmpty(state) || TextUtils.isEmpty(pincode))
            {
                Toast.makeText(
                    this.context,
                    "Please fill up all the missing fields",
                    Toast.LENGTH_LONG).show()
            }
            else{
                addNewContact()
                saveNewContacts()
                resetLayout()
                findNavController().navigate(R.id.panUpload)
            }
        }

        //layout.textView31.setOnClickListener { findNavController().navigate(R.id.panUpload) }
        //layout.textView32.setOnClickListener { findNavController().navigate(R.id.verificationcontact) }

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
        layout.add_veri_address_line1.setText("")
        layout.add_veri_address_line2.setText("")
        layout.add_veri_address_city.setText("")
        layout.add_veri_address_state.setText("")
        layout.add_veri_address_pin.setText("")
    }

    private fun saveNewContacts() {
        viewModel.setVerificationContact(updates)
    }
}