package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Contact
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_contact_bottom_sheet.view.*

class AddContactBottomSheetFragment: BottomSheetDialogFragment() {
    companion object {
        fun newInstance() = AddContactBottomSheetFragment()
    }

    lateinit var viewModel: ProfileViewModel
    lateinit var layout: View
    var updates: ArrayList<Contact> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.add_contact_bottom_sheet, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        layout.add_contact_add_more.setOnClickListener{
            addNewContact()
            resetLayout()
        }
        layout.add_contact_cancel.setOnClickListener{
            findNavController().navigate(R.id.aboutExpandedFragment)
        }
        layout.add_contact_save.setOnClickListener{
            addNewContact()
            saveNewContacts()
            findNavController().navigate(R.id.aboutExpandedFragment)
        }
    }

    private fun addNewContact() {
        updates.add(
            Contact(
                phone = layout.add_contact_phone.text.toString(),
                email = layout.add_contact_email.text.toString()
            )
        )
    }

    private fun resetLayout() {
        layout.add_contact_phone.setText("")
        layout.add_contact_email.setText("")
    }

    private fun saveNewContacts() {
        viewModel.setProfileContact(updates)
    }
}