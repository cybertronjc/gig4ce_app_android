package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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

    private var verificationId: String = ""
    private var param2: String = ""
    lateinit var layout: View
    lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString("verificationId")!!
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("VerifyOTP", "a " + verificationId.toString())
        Log.d("RANDOM", "yogesh")
        viewModel = ViewModelProviders.of(this.activity!!).get(LoginViewModel::class.java)
        viewModel.verificationId = verificationId.toString()
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