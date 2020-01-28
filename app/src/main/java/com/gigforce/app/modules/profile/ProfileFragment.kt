package com.gigforce.app.modules.profile

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.gigforce.app.R
import com.gigforce.app.utils.GlideApp
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    companion object {
        fun newInstance() = ProfileFragment()
    }

    val viewModel: ProfileViewModel = activityViewModels<ProfileViewModel>().value

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("DEBUG", "ENTERED PROFILE VIEW")
        return inflater.inflate(R.layout.fragment_profile_main_expanded, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //loadProfileImage()
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // viewModel =  // ViewModelProviders.of(this).get(ProfileViewModel::class.java)
    }

//    fun loadProfileImage() {
//        val gsReference = FirebaseStorage.getInstance()
//            .getReferenceFromUrl("gs://gigforce-dev.appspot.com/profile_pics/test.jpeg")
//
//        GlideApp.with(this.context!!)
//            .load(gsReference)
//            .placeholder(R.drawable.placeholder_user)
//            .centerCrop()
//            .into(img_profile)
//    }

}