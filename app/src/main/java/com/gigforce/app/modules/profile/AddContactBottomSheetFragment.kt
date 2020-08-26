package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Contact
import com.gigforce.app.modules.profile.models.ContactEmail
import com.gigforce.app.modules.profile.models.ContactPhone
import com.gigforce.app.utils.StringConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.add_contact_bottom_sheet.*
import kotlinx.android.synthetic.main.add_contact_bottom_sheet.view.*

class AddContactBottomSheetFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(
            bundle: Bundle,
            callbacks: AddContactBottomSheetCallbacks
        ): AddContactBottomSheetFragment {
            val instance = AddContactBottomSheetFragment()
            instance.arguments = bundle
            instance.setCallbacks(callbacks)
            return instance
        }

        const val STATE_ADD_CONTACT = 1;
        const val STATE_ADD_EMAIL = 2;
        const val STATE_EDIT_CONTACT = 3;
        const val STATE_EDIT_EMAIL = 4;
    }

    lateinit var viewModel: ProfileViewModel
    lateinit var layout: View
    private var callbacks: AddContactBottomSheetCallbacks? = null
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
        initUIAsPerBundle()
        layout.add_contact_add_more.setOnClickListener {

        }
        layout.add_contact_cancel.setOnClickListener {
            this.dismiss()
        }
        layout.add_contact_save.setOnClickListener {

            when (arguments?.getInt(StringConstants.CONTACT_EDIT_STATE.value)!!) {
                STATE_EDIT_CONTACT -> {
                    callbacks?.contactEdit(
                        ContactPhone(
                            add_contact_phone.text.toString(),
                            false,
                            cb_is_whatsapp_number_card_row.isChecked
                        ), false
                    )
                }
                STATE_ADD_CONTACT -> {
                    callbacks?.contactEdit(
                        ContactPhone(
                            add_contact_phone.text.toString(),
                            false,
                            cb_is_whatsapp_number_card_row.isChecked
                        ), true
                    )
                }
                STATE_EDIT_EMAIL -> {
                    callbacks?.emailEdit(
                        ContactEmail(
                            add_contact_phone.text.toString(),
                            false
                        ), false
                    )
                }
                STATE_ADD_EMAIL -> {
                    callbacks?.emailEdit(
                        ContactEmail(
                            add_contact_phone.text.toString(),
                            false
                        ), true
                    )
                }

            }
            dismiss()
        }
    }

    private fun initUIAsPerBundle() {
        checkState(arguments?.getInt(StringConstants.CONTACT_EDIT_STATE.value)!!)

    }

    private fun checkState(state: Int) {
        when (state) {
            STATE_ADD_CONTACT -> {
                tv_heading_add_contact_bts.text = getString(R.string.add_contact)
                cb_is_whatsapp_number_card_row.visibility = View.VISIBLE
                add_contact_add_more.visibility = View.VISIBLE

            }
            STATE_ADD_EMAIL -> {
                tv_heading_add_contact_bts.text = getString(R.string.add_email)
                cb_is_whatsapp_number_card_row.visibility = View.GONE
                add_contact_add_more.visibility = View.VISIBLE


            }
            STATE_EDIT_CONTACT -> {
                tv_heading_add_contact_bts.text = getString(R.string.edit_contact)
                cb_is_whatsapp_number_card_row.visibility = View.VISIBLE
                add_contact_add_more.visibility = View.GONE


            }
            STATE_EDIT_EMAIL -> {
                tv_heading_add_contact_bts.text = getString(R.string.edit_email)
                cb_is_whatsapp_number_card_row.visibility = View.GONE
                add_contact_add_more.visibility = View.GONE


            }

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

    interface AddContactBottomSheetCallbacks {
        fun contactEdit(contact: ContactPhone, add: Boolean)
        fun emailEdit(contact: ContactEmail, add: Boolean)
    }

    fun setCallbacks(callbacks: AddContactBottomSheetCallbacks) {
        this.callbacks = callbacks;
    }


}