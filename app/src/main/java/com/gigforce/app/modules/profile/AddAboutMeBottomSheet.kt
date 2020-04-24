package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.add_about_bottom_sheet.*

class AddAboutMeBottomSheet: ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = AddAboutMeBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        return inflateView(R.layout.add_about_bottom_sheet, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            about_me_text.setText(profile.aboutMe)
        })

        cancel_button.setOnClickListener {
            findNavController().navigate(R.id.aboutExpandedFragment)
        }

        save_button.setOnClickListener {
            profileViewModel!!.setProfileAboutMe(about_me_text.text.toString())
            findNavController().navigate(R.id.aboutExpandedFragment)
        }
        super.onViewCreated(view, savedInstanceState)
    }
}