package com.gigforce.app.modules.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.verification.Verification
import com.gigforce.app.modules.profile.models.Contact
import com.gigforce.app.modules.verification.models.*
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_contact_bottom_sheet.view.*
import kotlinx.android.synthetic.main.layout_verification_contact.view.*

class AddVerificationContact : BottomSheetDialogFragment() {
        companion object {
            fun newInstance() = AddVerificationContact()
        }

        lateinit var viewModel: VerificationViewModel
        lateinit var layout: View
        var updates: ArrayList<Contact_Verification> = ArrayList()

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

            layout.add_veri_contact_address.setOnClickListener{
                addNewContact()
                resetLayout()
            }
            layout.add_veri_contact_cancel.setOnClickListener{
                findNavController().navigate(R.id.verification)
            }
            layout.add_veri_contact_save.setOnClickListener{
                addNewContact()
                saveNewContacts()
                findNavController().navigate(R.id.verification)
            }
        }

        private fun addNewContact() {
            updates.add(
                Contact_Verification(
                    address = layout.add_veri_contact_address.text.toString(),
                    phone = layout.add_veri_contact_phone.text.toString(),
                    email = layout.add_veri_contact_email.text.toString()
                )
            )
        }

        private fun resetLayout() {
            layout.add_veri_contact_address.setText("")
            layout.add_veri_contact_phone.setText("")
            layout.add_veri_contact_email.setText("")
        }

        private fun saveNewContacts() {
            viewModel.setVerificationContact(updates)
        }
}