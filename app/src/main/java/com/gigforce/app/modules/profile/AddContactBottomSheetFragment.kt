package com.gigforce.app.modules.profile

import android.os.Bundle
import android.text.InputFilter
import android.text.InputFilter.LengthFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.modules.profile.models.Contact
import com.gigforce.app.modules.profile.models.ContactEmail
import com.gigforce.app.modules.profile.models.ContactPhone
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.isValidMail
import com.gigforce.app.utils.isValidMobile
import kotlinx.android.synthetic.main.add_contact_bottom_sheet.*
import kotlinx.android.synthetic.main.add_contact_bottom_sheet.view.*


class AddContactBottomSheetFragment : ProfileBaseBottomSheetFragment() {
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
            contactOperationInit()
            add_contact_phone.setText("")
        }
        layout.add_contact_cancel.setOnClickListener {
            this.dismiss()
        }
        layout.add_contact_save.setOnClickListener(fun(_: View) {
            if (!contactOperationInit()) return
            dismiss()
        })
    }

    private fun contactOperationInit(): Boolean {
        if (!validateForm()) return false
        when (arguments?.getInt(StringConstants.CONTACT_EDIT_STATE.value)!!) {
            STATE_EDIT_CONTACT -> {
                callbacks?.contactEdit(
                    arguments?.getString(StringConstants.CONTACT_TO_EDIT.value)!!,
                    ContactPhone(
                        add_contact_phone.text.toString(),
                        false,
                        cb_is_whatsapp_number_add_contact.isChecked
                    ), false
                )
            }
            STATE_ADD_CONTACT -> {
                callbacks?.contactEdit(
                    null,
                    ContactPhone(
                        add_contact_phone.text.toString(),
                        false,
                        cb_is_whatsapp_number_add_contact.isChecked
                    ), true
                )
            }
            STATE_EDIT_EMAIL -> {
                callbacks?.emailEdit(
                    arguments?.getString(StringConstants.EMAIL_TO_EDIT.value)!!,
                    ContactEmail(
                        add_contact_phone.text.toString(),
                        false
                    ), false
                )
            }
            STATE_ADD_EMAIL -> {
                callbacks?.emailEdit(
                    null,
                    ContactEmail(
                        add_contact_phone.text.toString(),
                        false
                    ), true
                )
            }

        }
        return true
    }

    private fun validateForm(): Boolean {
        if (add_contact_phone.text?.isEmpty()!!) {
            showErrorText(
                getString(R.string.empty_string_validation),
                form_error,
                add_contact_phone

            )

            return false
        }
        when (arguments?.getInt(StringConstants.CONTACT_EDIT_STATE.value)) {
            STATE_EDIT_CONTACT, STATE_ADD_CONTACT -> {
                val isValidMobile = isValidMobile(add_contact_phone.text.toString())

                if (isValidMobile) hideError(form_error, add_contact_phone) else showErrorText(
                    getString(R.string.validation_phone),
                    form_error,
                    add_contact_phone
                )
                return (isValidMobile)

            }
            STATE_EDIT_EMAIL, STATE_ADD_EMAIL -> {
                val isValidEmail = isValidMail(add_contact_phone.text.toString())
                if (isValidEmail) hideError(form_error, add_contact_phone) else showErrorText(
                    getString(R.string.validation_email),
                    form_error,
                    add_contact_phone
                )
                return (isValidEmail)
            }
            else -> {
                return false
            }

        }
    }

    private fun initUIAsPerBundle() {
//        add_contact_phone.background.mutate().setColorFilter(ContextCompat.getColor(requireContext(), R.color.bottom_sheet_et), PorterDuff.Mode.SRC_ATOP);
        checkState(arguments?.getInt(StringConstants.CONTACT_EDIT_STATE.value)!!)

    }

    private fun checkState(state: Int) {
        when (state) {
            STATE_ADD_CONTACT -> {
                tv_heading_add_contact_bts.text = getString(R.string.add_contact)
                cb_is_whatsapp_number_add_contact.visibility = View.VISIBLE
                add_contact_add_more.visibility = View.VISIBLE
                add_contact_phone.filters = arrayOf<InputFilter>(LengthFilter(13))


            }
            STATE_ADD_EMAIL -> {
                tv_heading_add_contact_bts.text = getString(R.string.add_email)
                cb_is_whatsapp_number_add_contact.visibility = View.GONE
                add_contact_add_more.visibility = View.VISIBLE
                add_contact_phone.hint = getString(R.string.email_madatory)
                add_contact_phone.inputType = InputType.TYPE_CLASS_TEXT
                add_contact_phone.filters = arrayOf<InputFilter>(LengthFilter(255))


            }
            STATE_EDIT_CONTACT -> {
                tv_heading_add_contact_bts.text = getString(R.string.edit_contact)
                cb_is_whatsapp_number_add_contact.visibility = View.VISIBLE
                add_contact_add_more.visibility = View.GONE
                add_contact_phone.setText(arguments?.getString(StringConstants.CONTACT_TO_EDIT.value))
                cb_is_whatsapp_number_add_contact.isChecked =
                    arguments?.getBoolean(StringConstants.IS_WHATSAPP_NUMBER.value, false)!!
                add_contact_phone.filters = arrayOf<InputFilter>(LengthFilter(13))
                add_contact_phone.setSelection(add_contact_phone.text.toString().length)
                add_contact_phone.isEnabled =
                    !arguments?.getBoolean(StringConstants.IS_REGISTERED_NUMBER.value, false)!!


            }
            STATE_EDIT_EMAIL -> {
                tv_heading_add_contact_bts.text = getString(R.string.edit_email)
                add_contact_phone.setText(arguments?.getString(StringConstants.EMAIL_TO_EDIT.value))
                cb_is_whatsapp_number_add_contact.visibility = View.GONE
                add_contact_add_more.visibility = View.GONE
                add_contact_phone.hint = getString(R.string.email_madatory)
                add_contact_phone.inputType = InputType.TYPE_CLASS_TEXT
                add_contact_phone.filters = arrayOf<InputFilter>(LengthFilter(255))
                add_contact_phone.setSelection(add_contact_phone.text.toString().length)


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
        fun contactEdit(oldPhone: String?, contact: ContactPhone, add: Boolean)
        fun emailEdit(oldEmail: String?, contact: ContactEmail, add: Boolean)
    }

    fun setCallbacks(callbacks: AddContactBottomSheetCallbacks) {
        this.callbacks = callbacks;
    }


}