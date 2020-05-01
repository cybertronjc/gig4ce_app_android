package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import kotlinx.android.synthetic.main.card_row.view.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*

class AboutExpandedFragment: ProfileBaseFragment() {
    companion object {
        fun newInstance() = AboutExpandedFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requireActivity().onBackPressedDispatcher.addCallback(this) {
            findNavController().navigate(R.id.profileFragment)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        inflateView(R.layout.fragment_profile_about_expanded, inflater, container)
        getFragmentView().nav_bar.about_me_active = true
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)
        return getFragmentView()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
        setListeners()
    }

    private fun initialize() {
        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            bio_card.isBottomRemoved = profile.aboutMe.isNotEmpty()
            bio_card.hasContentTitles = false
            bio_card.nextDestination = R.id.addAboutMeBottomSheet
            bio_card.cardTitle = "Bio"
            bio_card.cardContent = if (profile.aboutMe != "") profile.aboutMe
                                    else this.context!!.getString(R.string.empty_about_me_text)
            bio_card.cardBottom = if (profile.aboutMe != "") ""
                                    else "Add Bio"

            var languageString = ""
            profile.Language?.let {
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
            language_card.cardBottom = "Add Language"

            var contactString = ""
            profile.Contact?.let {
                for (contact in it) {
                    contactString += "phone: " + contact.phone + "\n"
                    contactString += "email: " + contact.email + "\n\n"
                }
            }
            contact_card.hasContentTitles = false
            contact_card.cardTitle = "Contact"
            contact_card.cardContent = contactString
            contact_card.cardBottom = "Add Contact"

            if (contact_card.edit_button != null) {
                contact_card.edit_button.setOnClickListener {
                    showAddContactDialog()
                }
            }

            about_top_profile.userName = profile.name
            about_top_profile.imageName = profile.profileAvatarName
        })
    }

    private fun setListeners() {

        bio_card.card_bottom.setOnClickListener {
            this.findNavController().navigate(R.id.addAboutMeBottomSheet)
        }

        language_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addLanguageBottomSheetFragment)
        }

        contact_card.card_bottom.setOnClickListener {
            showAddContactDialog()
        }

    }

    fun showAddContactDialog() {
        MaterialDialog(this.context!!).show {
            title(text = "Update Contact Details")
            message(
                text = "To update these details the giger will need to re-upload their" +
                        " Aadhar card images and undergo the KYC verification process again. " +
                        "We recommend that you do not change the name or address details unless " +
                        "necessary"
            )
            positiveButton(text = "Proceed") {
                Toast.makeText(this.context!!, "Not Implemented", Toast.LENGTH_SHORT).show()
            }
            negativeButton(text = "Cancel") { }
        }
    }

    private fun getLanguageLevel(level: Int): String {
        return when (level) {
            in 0..25 -> "beginner"
            in 26..75 -> "moderate"
            else -> "advanced"
        }
    }

}
