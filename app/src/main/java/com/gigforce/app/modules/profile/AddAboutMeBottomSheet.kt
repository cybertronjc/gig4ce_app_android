package com.gigforce.app.modules.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import com.gigforce.app.R
import kotlinx.android.synthetic.main.add_about_bottom_sheet.*

class AddAboutMeBottomSheet : ProfileBaseBottomSheetFragment() {

    companion object {
        fun newInstance() = AddAboutMeBottomSheet()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflateView(R.layout.add_about_bottom_sheet, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        profileViewModel!!.userProfileData.observe(this, Observer { profile ->
            about_me_text.setText(profile?.aboutMe!!)
        })

        cancel_button.setOnClickListener {
            this.dismiss()

        }

        save_button.setOnClickListener {
            if (about_me_text.text.toString().isNotEmpty() && about_me_text.text.toString().trim()
                    .isEmpty()
            ) {
                form_error.visibility = View.VISIBLE
                about_me_text.setHintTextColor(resources.getColor(R.color.red))
            } else {
                profileViewModel!!.setProfileAboutMe(about_me_text.text.toString())
                this.dismiss()
            }
        }
        super.onViewCreated(view, savedInstanceState)
    }
}