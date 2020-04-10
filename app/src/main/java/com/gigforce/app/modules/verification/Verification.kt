package com.gigforce.app.modules.verification

import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
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
import java.util.regex.Matcher
import java.util.regex.Pattern

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

    private val ADDRESS =
        Pattern.compile("^(\\w+\\s*[\\#\\-\\,\\/\\.\\(\\)\\&]*)+")
    private val CITY_STATE =
        Pattern.compile("^(\\w+\\s*\\w*)+")
    private val PINCODE =
        Pattern.compile("^([0-9]{6}|[0-9]{3}\\s*[0-9]{3})")

    lateinit var match: Matcher;

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

    private fun validateFields(address1:String, address2:String, city:String, state:String, pincode:String):Boolean {
        //TODO Instead of toast msg we can put text msg on top of missing edit text or turn the edit text box to red!
        if (address1.isEmpty())
        {
            Toast.makeText(this.context, "Please enter address1", Toast.LENGTH_SHORT).show()
            layout.add_veri_address_line1.highlightColor = resources.getColor(R.color.colorAccent)
            return false
        }
        else if (address2.isEmpty())
        {
            Toast.makeText(this.context, "Please enter address2", Toast.LENGTH_SHORT).show()
            return false
        }
        else{
            match = ADDRESS.matcher("$address1 $address2");
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid address1 and address2", Toast.LENGTH_SHORT).show()
                Log.d("Verification: ", "$address1 $address2")
                return false
            }
        }
        if(city.isEmpty())
        {
            Toast.makeText(this.context, "Please enter city", Toast.LENGTH_SHORT).show()
            return false
        }
        else
        {
            match = CITY_STATE.matcher(city);
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid city", Toast.LENGTH_SHORT).show()
                Log.d("Verification: ", city)
                return false
            }
        }

        if(state.isEmpty())
        {
            Toast.makeText(this.context, "Please enter state", Toast.LENGTH_SHORT).show()
            return false
        }
        else
        {
            match = CITY_STATE.matcher(state);
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid state", Toast.LENGTH_SHORT).show()
                Log.d("Verification: ", state)
                return false
            }
        }

        if(pincode.isEmpty())
        {
            Toast.makeText(this.context, "Please enter pincode", Toast.LENGTH_SHORT).show()
            return false
        }
        else
        {
            match = PINCODE.matcher(pincode);
            if(!match.matches()) {
                Toast.makeText(this.context, "Please enter valid pincode", Toast.LENGTH_SHORT).show()
                Log.d("Verification: ", pincode)
                return false
            }
        }

        return true;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

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

            var areValid = validateFields(address1, address2, city, state, pincode);
            //if(TextUtils.isEmpty(address1) || TextUtils.isEmpty(address2) || TextUtils.isEmpty(city) || TextUtils.isEmpty(state) || TextUtils.isEmpty(pincode))
            // TODO Is this check needed?
            if(!areValid)
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
                //findNavController().navigate(R.id.aadhaarUpload)
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