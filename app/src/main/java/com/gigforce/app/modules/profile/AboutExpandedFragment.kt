package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.afollestad.materialdialogs.MaterialDialog
import com.gigforce.app.R
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.card_row.view.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.nav_bar
import kotlinx.android.synthetic.main.fragment_profile_experience_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*
import kotlinx.android.synthetic.main.profile_nav_bar.view.*

class AboutExpandedFragment: Fragment() {
    companion object {
        fun newInstance() = AboutExpandedFragment()
    }

    lateinit var storage: FirebaseStorage
    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel

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
        layout = inflater.inflate(R.layout.fragment_profile_about_expanded, container, false)
        layout.nav_bar.about_me_active = true
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = FirebaseStorage.getInstance()
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)


        viewModel.userProfileData.observe(this, Observer { profile ->
            layout.bio_card.isBottomRemoved = profile.aboutMe.isNotEmpty()
            layout.bio_card.hasContentTitles = false
            layout.bio_card.nextDestination = R.id.addAboutMeBottomSheet
            layout.bio_card.cardTitle = "Bio"
            layout.bio_card.cardContent = if (profile.aboutMe != "") profile.aboutMe
                                          else this.context!!.getString(R.string.empty_about_me_text)
            layout.bio_card.cardBottom = if (profile.aboutMe != "") ""
                                         else "Add Bio"

            var languageString = ""
            profile.languages?.let {
                val languages = it.sortedByDescending { language -> language.speakingSkill }
                for (lang in languages) {
                    languageString += lang.name + "\n"
                    languageString += "Speaking " + getLanguageLevel(lang.speakingSkill.toInt()) + "\n"
                    languageString += "Writing " + getLanguageLevel(lang.writingSkill.toInt()) + "\n\n"
                }
            }
            layout.language_card.nextDestination = R.id.editLanguageBottomSheet
            layout.language_card.cardTitle = "Language"
            layout.language_card.cardContent = languageString
            layout.language_card.cardBottom = "Add Language"

            var contactString = ""
            profile.contact?.let {
                for (contact in it) {
                    contactString += "phone: " + contact.phone + "\n"
                    contactString += "email: " + contact.email + "\n\n"
                }
            }
            layout.contact_card.hasContentTitles = false
            layout.contact_card.cardTitle = "Contact"
            layout.contact_card.cardContent = contactString
            layout.contact_card.cardBottom = "Add Contact"

            if (layout.contact_card.edit_button != null) {
                layout.contact_card.edit_button.setOnClickListener {
                    showAddContactDialog()
                }
            }

            layout.about_top_profile.userName = profile.name
            layout.about_top_profile.imageName = profile.profileAvatarName
        })

        layout.bio_card.card_bottom.setOnClickListener {
            this.findNavController().navigate(R.id.addAboutMeBottomSheet)
        }

        layout.language_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addLanguageBottomSheetFragment)
        }

        layout.contact_card.card_bottom.setOnClickListener{
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
