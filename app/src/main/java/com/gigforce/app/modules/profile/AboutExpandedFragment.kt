package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.request.RequestOptions
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import kotlinx.android.synthetic.main.profile_card_background.view.*
import kotlinx.android.synthetic.main.top_profile_bar.view.*

class AboutExpandedFragment: Fragment() {
    companion object {
        fun newInstance() = AboutExpandedFragment()
    }

    lateinit var storage: FirebaseStorage
    lateinit var layout: View
    lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.fragment_profile_about_expanded, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        storage = FirebaseStorage.getInstance()
        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)


        viewModel.userProfileData.observe(this, Observer { profile ->
            layout.bio_card.cardTitle = "Bio"
            layout.bio_card.cardContent = profile.bio

            var languageString = ""
            for (lang in profile.Language!!) {
                languageString += lang.name + "\n"
                languageString += "Speaking " + getLanguageLevel(lang.speakingSkill.toInt()) + "\n"
                languageString += "Writing " + getLanguageLevel(lang.writingSkill.toInt()) + "\n\n"
            }
            layout.language_card.nextDestination = R.id.editLanguageBottomSheet
            layout.language_card.cardTitle = "Language"
            layout.language_card.cardContent = languageString
            layout.language_card.cardBottom = "+ Add Language"

            var contactString = ""
            for (contact in profile.Contact!!) {
                contactString += "phone: " + contact.phone + "\n"
                contactString += "email: " + contact.email + "\n\n"
            }
            layout.contact_card.cardTitle = "Contact"
            layout.contact_card.cardContent = contactString
            layout.contact_card.cardBottom = "+ Add Contact"

            layout.about_top_profile.userName = profile.name
            layout.about_top_profile.imageName = profile.profileAvatarName
        })

        layout.language_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addLanguageBottomSheetFragment)
        }

        layout.contact_card.card_bottom.setOnClickListener{
            this.findNavController().navigate(R.id.addContactBottomSheetFragment)
        }

//        layout.about_expanded_back_button.setOnClickListener{
//            this.findNavController().navigate(R.id.profileFragment)
//        }

    }

    private fun getLanguageLevel(level: Int): String {
        if (level <= 25) {
            return "beginner"
        }
        else if (level <= 75) {
            return "moderate"
        }
        else {
            return "advanced"
        }
    }

}