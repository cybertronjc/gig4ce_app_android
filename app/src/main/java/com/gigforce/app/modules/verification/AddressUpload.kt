package com.gigforce.app.modules.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.verification.models.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_verification.view.*

class AddressUpload : BottomSheetDialogFragment() {
        companion object {
            fun newInstance() = AddressUpload()
        }

        lateinit var viewModel: VerificationViewModel
        lateinit var layout: View
        var updates: ArrayList<Address> = ArrayList()

        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            layout = inflater.inflate(R.layout.layout_verification_contact, container, false)
            return layout
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

//            layout.add_veri_contact_address.setOnClickListener{
//                addNewContact()
//                resetLayout()
//            }
            layout.button_veri_address_cancel.setOnClickListener{
                findNavController().navigate(R.id.verification)
            }
            layout.button_veri_address_save.setOnClickListener{
                addNewContact()
                saveNewContacts()
                findNavController().navigate(R.id.verification)
            }
        }

    private fun addNewContact() {
        updates.add(
            Address(
                address = layout.add_veri_address_line1.text.toString()+" "+layout.add_veri_address_line2.text.toString(),
                city = layout.add_veri_address_city.text.toString(),
                state = layout.add_veri_address_state.text.toString(),
                pincode = layout.add_veri_address_pin.text.toString()
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