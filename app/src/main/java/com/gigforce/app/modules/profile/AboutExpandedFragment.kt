package com.gigforce.app.modules.profile

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.core.base.dialog.ConfirmationDialogOnClickListener
import com.gigforce.app.modules.landingscreen.LandingPageConstants.INTENT_EXTRA_ACTION
import com.gigforce.app.modules.landingscreen.LandingPageConstants.INTENT_EXTRA_CAME_FROM_LANDING_SCREEN
import com.gigforce.app.modules.profile.models.ContactEmail
import com.gigforce.app.modules.profile.models.ContactPhone
import com.gigforce.app.modules.profile.models.ProfileData
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.ViewModelProviderFactory
import kotlinx.android.synthetic.main.contact_edit_warning_dialog.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*

class AboutExpandedFragment : ProfileBaseFragment(), ProfileCardBgCallbacks,
    AddContactBottomSheetFragment.AddContactBottomSheetCallbacks {
    companion object {
        fun newInstance() = AboutExpandedFragment()


        const val ACTION_OPEN_EDIT_ABOUT_ME_BOTTOM_SHEET = 31
        const val ACTION_OPEN_EDIT_LANGUAGE_BOTTOM_SHEET = 32
    }

    private val viewModelFactory by lazy {
        ViewModelProviderFactory(ViewModelAboutExpandedFragment(ModelAboutExpandedFragment()))
    }
    private val viewModel: ViewModelAboutExpandedFragment by lazy {
        ViewModelProvider(this, viewModelFactory).get(ViewModelAboutExpandedFragment::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        requireActivity().onBackPressedDispatcher.addCallback(this) {
//            findNavController().navigate(R.id.profileFragment)
//        }
    }

    private var cameFromLandingPage = false
    private var action: Int = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        viewModel.updateEmail( profileViewModel.userProfileData.value?.id!!)
        arguments?.let {
            cameFromLandingPage = it.getBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN)
            action = it.getInt(INTENT_EXTRA_ACTION)
        }

        savedInstanceState?.let {
            cameFromLandingPage = it.getBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN)
        }

        var view = inflateView(R.layout.fragment_profile_about_expanded, inflater, container)
        view?.nav_bar?.about_me_active = true
        return view
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(INTENT_EXTRA_CAME_FROM_LANDING_SCREEN, cameFromLandingPage)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contact_card.showIsWhatsappCb = true
        contact_card.setCallbacks(this)
        email_card.setCallbacks(this)
        initialize()
        setListeners()

    }

    private fun initialize() {
        profileViewModel.userProfileData.observe(viewLifecycleOwner, Observer { profileObs ->
            loadProfile(profileObs)
        })

        if (cameFromLandingPage)
            profileViewModel.getProfileData()

        when (action) {
            ACTION_OPEN_EDIT_ABOUT_ME_BOTTOM_SHEET -> {
                this.findNavController().navigate(R.id.addAboutMeBottomSheet)
            }
            ACTION_OPEN_EDIT_LANGUAGE_BOTTOM_SHEET -> {
                this.findNavController().navigate(R.id.addLanguageBottomSheetFragment)
            }
        }
        viewModel.observableReloadProfile.observe(viewLifecycleOwner, Observer {
            if (it!!) {
                loadProfile(
                    profileViewModel.userProfileData.value!!
                )
            } else {
                profileViewModel.getProfileData()
            }

        })
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            showToast(it!!)

        })
    }

    private fun loadProfile(profile: ProfileData) {
        contact_card.contactNumbers.clear()
        email_card.emails.clear()
        contact_card.setWhatsAppChecked.clear()
        if (profile.contactPhone == null || profile.contactPhone?.isEmpty() == true)
            viewModel.updateContactDetails(
                arguments?.getString(StringConstants.PROFILE_ID.value) ?: "",
                profile.contact!!
            )

        bio_card.isBottomRemoved = profile.aboutMe.isNotEmpty()
        bio_card.hasContentTitles = false
        bio_card.nextDestination = R.id.addAboutMeBottomSheet

        bio_card.cardTitle = "Bio"
        bio_card.cardContent = if (profile.aboutMe != "") profile.aboutMe
        else this.requireContext().getString(R.string.empty_about_me_text)
        bio_card.cardBottom = if (profile.aboutMe != "") ""
        else "Add bio"

        var languageString = ""
        profile.languages?.let {
            val languages = it.sortedByDescending { language -> language.speakingSkill }
            for (lang in languages) {
                languageString += lang.name + "\n"
                languageString += "Speaking " + getLanguageLevel(lang.speakingSkill.toInt()) + "\n"
                languageString += "Writing " + getLanguageLevel(lang.writingSkill.toInt()) + "\n\n"
            }
        }
        language_card.nextDestination = R.id.editLanguageBottomSheet
        language_card.cardTitle = "Language"
        language_card.cardContent = languageString
        language_card.cardBottom = "Add languages"

        var contactString = ""
        profile.contactPhone?.let {
            for (contactPhone in it) {
                contact_card.contactNumbers.add(contactPhone.phone ?: "")
                contactString += getString(R.string.contact_hyphen) + " " + contactPhone.phone + "\n\n"
                if (contact_card.showIsWhatsappCb) {
                    contact_card.setWhatsAppChecked.add(contactPhone.isWhatsapp)
                }
            }
        }
        contact_card.hasContentTitles = false
        contact_card.cardTitle = getString(R.string.contact)
        contact_card.cardContent = contactString
        contact_card.cardBottom = getString(R.string.add_contacts)

//        if (contact_card.edit_button != null) {
//            contact_card.edit_button.setOnClickListener {
//                showAddContactDialog(false)
//            }
//        }
        var email = ""
        profile.contactEmail?.let {
            for (contactEmail in it) {
                email_card.emails.add(contactEmail.email ?: "")
                email += getString(R.string.email_hyphen) + " " + contactEmail.email + "\n\n"
//                if (contact_card.showIsWhatsappCb) {
//                    contact_card.setWhatsAppChecked.add(contactEmail.isWhatsapp)
//                }
            }
        }
        email_card.hasContentTitles = false
        email_card.cardTitle = getString(R.string.emails)
        email_card.cardContent = email
        email_card.cardBottom = getString(R.string.add_email)

//        if (email_card.edit_button != null) {
//            email_card.edit_button.setOnClickListener {
//                showAddContactDialog(true)
//            }
//        }

        about_top_profile.userName = profile.name
        about_top_profile.imageName = profile.profileAvatarName
//        profileViewModel.listener?.remove()
    }

    private fun setListeners() {

        bio_card.card_bottom.setOnClickListener {
            this.findNavController().navigate(R.id.addAboutMeBottomSheet)
        }

        language_card.card_bottom.setOnClickListener {
            this.findNavController().navigate(R.id.addLanguageBottomSheetFragment)
        }

        contact_card.card_bottom.setOnClickListener {
            AddContactBottomSheetFragment.newInstance(
                bundleOf(
                    StringConstants.CONTACT_EDIT_STATE.value to AddContactBottomSheetFragment.STATE_ADD_CONTACT
                )
                , this
            ).show(parentFragmentManager, AddContactBottomSheetFragment::class.java.name)
        }
        email_card.card_bottom.setOnClickListener {
            AddContactBottomSheetFragment.newInstance(
                bundleOf(
                    StringConstants.CONTACT_EDIT_STATE.value to AddContactBottomSheetFragment.STATE_ADD_EMAIL
                )
                , this
            ).show(parentFragmentManager, AddContactBottomSheetFragment::class.java.name)
        }

    }

    private fun showAddContactDialog(isEmail: Boolean, registered: Boolean, bundle: Bundle) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.contact_edit_warning_dialog)
        val window = dialog.getWindow();

        window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.cancel_button.setOnClickListener {
            dialog.dismiss()
        }


        dialog.submit_button.setOnClickListener {
            dialog.dismiss()
            AddContactBottomSheetFragment.newInstance(
                if (registered) bundle else bundleOf(
                    StringConstants.CONTACT_EDIT_STATE.value to if (isEmail) AddContactBottomSheetFragment.STATE_ADD_EMAIL else AddContactBottomSheetFragment.STATE_ADD_CONTACT
                )
                , this
            ).show(parentFragmentManager, AddContactBottomSheetFragment::class.java.name)
        }

        dialog.show()
    }

    private fun getLanguageLevel(level: Int): String {
        return when (level) {
            in 0..25 -> "beginner"
            in 26..75 -> "moderate"
            else -> "advanced"
        }
    }

    override fun checked(isChecked: Boolean, contact: String) {

        showConfirmationDialogType7(
            getString(R.string.this_is_my_whatsapp_number),
            object : ConfirmationDialogOnClickListener {
                override fun clickedOnYes(dialog: Dialog?) {
                    viewModel.setWhatsAppNumberStatus(
                        profileViewModel.userProfileData.value?.id!!,
                        profileViewModel.userProfileData.value?.contactPhone!!,
                        contact,
                        true
                    )
                    dialog?.dismiss()
                }

                override fun clickedOnNo(dialog: Dialog?) {
                    viewModel.setWhatsAppNumberStatus(
                        profileViewModel.userProfileData.value?.id!!,
                        profileViewModel.userProfileData.value?.contactPhone!!,
                        contact,
                        false
                    )
                    dialog?.dismiss()
                }

            })


    }

    override fun editNumber(number: String, isWhatsApp: Boolean, isRegistered: Boolean) {
        val bundle = bundleOf(
            StringConstants.CONTACT_EDIT_STATE.value to AddContactBottomSheetFragment.STATE_EDIT_CONTACT,
            StringConstants.CONTACT_TO_EDIT.value to number,
            StringConstants.IS_WHATSAPP_NUMBER.value to isWhatsApp,
            StringConstants.IS_REGISTERED_NUMBER.value to isRegistered

        )
        if (isRegistered) {
            showAddContactDialog(false, isRegistered, bundle)
        } else {
            AddContactBottomSheetFragment.newInstance(
                bundle
                , this
            ).show(parentFragmentManager, AddContactBottomSheetFragment::class.java.name)
        }


    }

    override fun editEmail(email: String) {
        AddContactBottomSheetFragment.newInstance(
            bundleOf(
                StringConstants.CONTACT_EDIT_STATE.value to AddContactBottomSheetFragment.STATE_EDIT_EMAIL,
                StringConstants.EMAIL_TO_EDIT.value to email
            )
            , this
        ).show(parentFragmentManager, AddContactBottomSheetFragment::class.java.name)
    }


    override fun contactEdit(oldPhone: String?, contact: ContactPhone, add: Boolean) {
        viewModel.contactEdit(
            profileViewModel.userProfileData.value?.id!!,
            oldPhone,
            profileViewModel.userProfileData.value?.contactPhone ?: arrayListOf(),
            contact,
            add
        )
    }

    override fun emailEdit(oldEmail: String?, contact: ContactEmail, add: Boolean) {
        viewModel.emailEdit(
            profileViewModel.userProfileData.value?.id!!,
            oldEmail,
            profileViewModel.userProfileData.value?.contactEmail ?: arrayListOf(),
            contact,
            add
        )
    }


}
