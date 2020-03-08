package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_login.*

const val TAG = "fragment/login"

class LoginFragment : Fragment() {

    var storedVerificationId:String = ""
    var resendToken:PhoneAuthProvider.ForceResendingToken? = null

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    private fun onConfirmClicked() {
        val phoneNo = edit_phone.text.toString()
        viewModel.sendVerificationCode(phoneNo)
    }

    fun navigateToConfirmOTPScreen() {
        this.findNavController().navigate(
            LoginFragmentDirections.actionLoginFragmentToConfirmOtpFragment()
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        btn_confirm.setOnClickListener { onConfirmClicked() }

        viewModel = this.activityViewModels<LoginViewModel>().value
        viewModel.activity = this.activity!!

        val navController = findNavController()

        requireActivity().onBackPressedDispatcher.addCallback {
            // navController.popBackStack(R.id.homeFragment, false)
            // Do Nothing on back!
            // todo: experience need to improve.
        }

        viewModel.liveState.observeForever {
            when(it){
                LoginViewModel.STATE_CODE_SENT -> navigateToConfirmOTPScreen()
                LoginViewModel.STATE_SIGNIN_SUCCESS -> findNavController().popBackStack()
                else -> {

                }
            }
        }
    }

}