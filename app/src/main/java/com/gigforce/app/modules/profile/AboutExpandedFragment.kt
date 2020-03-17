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
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.profile_nav_to_education
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*

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
            layout.about_exp_about_content.text = profile.bio

            var languageString = ""
            for (lang in profile.Language!!) {
                languageString += lang.name + "\n"
                languageString += "Speaking " + lang.speakingSkill + "\n"
                languageString += "Writing " + lang.writingSkill + "\n\n"
            }
            layout.about_exp_language_content.text = languageString

            var contactString = ""
            for (contact in profile.Contact!!) {
                contactString += "phone: " + contact.phone + "\n"
                contactString += "email: " + contact.email + "\n\n"
            }
            layout.about_expanded_contact_text.text = contactString

            layout.about_top_profile.userName = profile.name
            layout.about_top_profile.imageName = "ysharma.jpg"
        })

        layout.add_language_button.setOnClickListener{
            this.findNavController().navigate(R.id.addLanguageBottomSheetFragment)
        }

        layout.add_contact_button.setOnClickListener{
            this.findNavController().navigate(R.id.addContactBottomSheetFragment)
        }

//        layout.about_expanded_back_button.setOnClickListener{
//            this.findNavController().navigate(R.id.profileFragment)
//        }

        layout.profile_nav_to_education.setOnClickListener{
            this.findNavController().navigate(R.id.educationExpandedFragment)
        }

        layout.profile_nav_to_experience.setOnClickListener{
            this.findNavController().navigate(R.id.experienceExpandedFragment)
        }


    }

}