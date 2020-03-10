package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.gigforce.app.modules.profile.ProfileViewModel
import kotlinx.android.synthetic.main.mobile_number_input.view.*

class MobileInput: Fragment() {
    companion object {
        fun newInstance() = MobileInput()
    }

    lateinit var layout: View
    lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        layout = inflater.inflate(R.layout.mobile_number_input, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.activity = this.activity!!

        layout.send_otp_button.setOnClickListener{
            Log.d("LoginDebug", layout.otp_mobile_number.text.toString())
            viewModel.sendVerificationCode(layout.otp_mobile_number.text.toString())
        }

        viewModel.liveState.observeForever {
            when(it){
                LoginViewModel.STATE_CODE_SENT -> findNavController().navigate(R.id.verifyOTP)
                LoginViewModel.STATE_SIGNIN_SUCCESS -> findNavController().popBackStack()
                else -> {

                }
            }
        }
    }
}