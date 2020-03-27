package com.gigforce.app.modules.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.auth.ui.main.LoginViewModel
import com.gigforce.app.modules.auth.ui.main.Login
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.modules.verification.models.Contact_Verification
import com.gigforce.app.modules.verification.models.VerificationData
import kotlinx.android.synthetic.main.fragment_profile_about_expanded.view.*
import kotlinx.android.synthetic.main.fragment_video_resume.*
import kotlinx.android.synthetic.main.layout_verification.view.*
import kotlinx.android.synthetic.main.layout_verification_contact.*
import kotlinx.android.synthetic.main.layout_verification_contact.view.*

class Verification: Fragment() {
    companion object {
        fun newInstance() = Login()
    }

    lateinit var layout: View
    lateinit var viewModel: VerificationViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)
        layout = inflater.inflate(R.layout.layout_verification, container, false)
        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
        return layout
    }

    val callback: OnBackPressedCallback =
        object : OnBackPressedCallback(true /* enabled by default */) {
            override fun handleOnBackPressed() { // Handle the back button event
                onBackPressed()
            }
        }

    fun onBackPressed() {
            findNavController().popBackStack()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(VerificationViewModel::class.java)

//        viewModel.veriData.observe(this, Observer { contact ->
//            var contactString = ""
//            //for (contact in contacts!!) {
//                contactString += "address: "+contact.address +"\n"
//                contactString += "phone: " + contact.phone + "\n"
//                contactString += "email: " + contact.email + "\n\n"
//            //}
//            //layout.add_veri_contact_address.text = contact.address
//        })

        layout.textView32.setOnClickListener { findNavController().navigate(R.id.verificationcontact) }

    }
}