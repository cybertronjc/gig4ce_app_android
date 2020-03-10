package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import kotlinx.android.synthetic.main.fragment_confirm_otp.*
import kotlinx.android.synthetic.main.otp_verification.view.*

class VerifyOTP: Fragment() {
    private var ARG_PARAM1 = "verificationId"
    private var ARG_PARAM2 = "param2"

    companion object {
        fun newInstance() = VerifyOTP()
        fun newInstance(param1: String, param2: String) =
            VerifyOTP().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private var verificationId: String? = null
    private var param2: String? = null
    lateinit var layout: View
    lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this.activity!!).get(LoginViewModel::class.java)
        layout = inflater.inflate(R.layout.otp_verification, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.verify_otp_button.setOnClickListener {
            Log.d("Debug", layout.otp_string.text.toString())
            viewModel.verifyPhoneNumberWithCode(layout.otp_string.text.toString())
            Log.d("Status","Login Success");
            findNavController().navigate(R.id.profileFragment)
        }
    }
}