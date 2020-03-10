package com.gigforce.app.modules.auth.ui.main

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.android.synthetic.main.fragment_confirm_otp.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "verificationId"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ConfirmOtpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConfirmOtpFragment : Fragment() {

    // TODO: Rename and change types of parameters
    private var verificationId: String? = null
    private var param2: String? = null

    lateinit var viewModel:LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            verificationId = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_confirm_otp, container, false)
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this.activity!!).get(LoginViewModel::class.java)
        btn_confirm.setOnClickListener { onConfirmClicked();Log.d("Login sucess!!!!!!!!!","asdfasdfas");
            //findNavController().navigate(R.id.OBSlidesFragment) }
            findNavController().navigate(R.id.profileFragment) }
        requireActivity().onBackPressedDispatcher.addCallback {
            // todo: experience need to improve.
            findNavController().popBackStack(R.id.loginFragment,false);
        }
    }

    private fun onConfirmClicked() {
        val otp = edit_otp.text.toString()
        viewModel.verifyPhoneNumberWithCode(otp)
        /*
        on success or failure
         */
        Toast.makeText(this.context, "Code Submitted For Confirmation", Toast.LENGTH_SHORT).show()
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConfirmOtpFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic fun newInstance(param1: String, param2: String) =
                ConfirmOtpFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}