package com.gigforce.app.modules.profile

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.photocrop.PhotoCrop
import com.gigforce.app.utils.GlideApp
import com.google.android.material.chip.Chip
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_profile_education_expanded.view.*
import kotlinx.android.synthetic.main.fragment_profile_main_expanded.view.*
import kotlinx.android.synthetic.main.profile_main_card_background.view.*
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    private lateinit var viewModel: ProfileViewModel
    private lateinit var storage: FirebaseStorage
    private lateinit var layout: View
    private lateinit var profileAvatarName: String
    private var PHOTO_CROP: Int = 45
    private var PROFILE_PICTURE_FOLDER: String = "profile_pics"


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        storage = FirebaseStorage.getInstance()
        Log.d("DEBUG", "ENTERED PROFILE VIEW")
        layout = inflater.inflate(R.layout.fragment_profile_main_expanded, container, false)

        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        // load user data
        viewModel.userProfileData.observe(this, Observer { profile ->
            layout.gigger_rating.text = profile.rating!!.getTotal().toString()
            layout.task_done.text = profile.tasksDone.toString()
            layout.connection_count.text = profile.connections.toString()
            layout.main_expanded_user_name.text = profile.name

            Log.d("ProfileFragment", profile.isVerified.toString())
            if (profile.isVerified) {
                layout.main_expanded_is_verified.setBackgroundColor(Color.parseColor("#00FF00"))
            }

            var tagsString = ""
            for (tag in profile.Tags!!) {
                var chip = Chip(this.context)
                chip.text = " $tag "
                chip.isClickable = false
                layout.main_tags.addView(chip)
            }
            //layout.main_tags.text = tagsString

            var educationString = ""
            var format = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            for (education in profile.Education!!) {
                educationString += education.institution + "\n"
                educationString += education.degree + " - " + education.course + "\n"
                educationString += format.format(education.startYear!!) + " - " + format.format(education.endYear!!) + "\n\n"
            }
            Log.d("ProfileFragment", educationString)
            layout.main_education_card.card_title.text = "Education"
            layout.main_education_card.card_content.text = educationString
            layout.main_education_card.card_view_more.setOnClickListener {
                findNavController().navigate(R.id.educationExpandedFragment)
            }

            var experienceString = ""
            for (exp in profile.Experience!!) {
                experienceString += exp.title + "\n"
                experienceString += exp.employmentType + "\n"
                experienceString += exp.location + "\n"
                experienceString += format.format(exp.startDate!!) + "-" + format.format(exp.endDate!!) + "\n\n"
            }
            layout.main_experience_card.card_title.text = "Experience"
            layout.main_experience_card.card_content.text = experienceString
            layout.main_experience_card.card_view_more.setOnClickListener {
                findNavController().navigate(R.id.experienceExpandedFragment)
            }

            layout.main_about_card.card_title.text = "About me"
            layout.main_about_card.card_content.text = profile.bio.toString()
            layout.main_about_card.card_view_more.setOnClickListener {
                findNavController().navigate(R.id.aboutExpandedFragment)
            }
            Log.d("ProfileFragment", profile.rating.toString())

            profileAvatarName = profile.profileAvatarName
            Log.e("PROFILE AVATAR", profileAvatarName)
            if (profileAvatarName != null)
                loadImage(profileAvatarName)
        })


        /*
        Clicking on profile picture opens Photo Crop Activity
         */
        layout.profile_avatar.setOnClickListener {
            val photoCropIntent = Intent(context, PhotoCrop::class.java)
            photoCropIntent.putExtra("purpose","profilePictureCrop")
            photoCropIntent.putExtra("uid",viewModel.uid)
            photoCropIntent.putExtra("fbDir", "/profile_pics/")
            photoCropIntent.putExtra("detectFace",1)
            photoCropIntent.putExtra("folder", PROFILE_PICTURE_FOLDER)
            photoCropIntent.putExtra("file", profileAvatarName)
            startActivityForResult(photoCropIntent, PHOTO_CROP)
        }
        layout.add_tags_button.setOnClickListener{
            this.findNavController().navigate(R.id.addTagBottomSheet)
        }

        // back page navigation
        layout.profile_main_expanded_back_button.setOnClickListener{
            this.findNavController().navigate(R.id.homeFragment)
        }
    }

    private fun loadImage(Path: String) {
        var profilePicRef: StorageReference =storage.reference.child(PROFILE_PICTURE_FOLDER).child(Path)
        GlideApp.with(this.context!!)
                .load(profilePicRef)
                .into(layout.profile_avatar)
    }

    override fun onActivityResult(
            requestCode: Int,
            resultCode: Int,
            data: Intent?
    ): Unit {

        super.onActivityResult(requestCode, resultCode, data)

        /*
        For photo crop. The activity returns the the filename with which the cropped photo
        is saved on firestore. The name is updated in profile information and the new
        photo is loaded in the view
        */
        if (requestCode == PHOTO_CROP && resultCode == Activity.RESULT_OK) {
            var imageName: String? = data?.getStringExtra("filename")
            Log.v("PROFILE_FRAG_OAR", "filename is:" + imageName)
            if (null != imageName) {
                loadImage(imageName)
            }
        }
    }

}
