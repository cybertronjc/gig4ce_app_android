package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.verify_mobile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.verification.UtilMethods
import com.gigforce.app.utils.Lce
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_ambsd_check_mobile.*

class CheckMobileFragment : BaseFragment() {

    private val viewModel: VerifyUserMobileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_ambsd_check_mobile, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initListeners()
        initViewModel()
    }

    private fun initListeners() {
        submitBtn.setOnClickListener {
            validateDataAndsubmit()
        }
    }

    private fun validateDataAndsubmit() {
        if (mobile_no_et.text.length != 10) {
            showAlertDialog("", "Enter a valid Mobile No")
            return
        }

        viewModel.checkMobileNo(
            "+91${mobile_no_et.text}"
        )
    }

    private fun showAlertDialog(title: String, message: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Okay") { _, _ -> }
            .show()
    }

    private fun initViewModel() {
        viewModel.checkMobileNo
            .observe(viewLifecycleOwner, androidx.lifecycle.Observer {

                when (it) {
                    Lce.Loading -> {
                        UtilMethods.showLoading(requireContext())
                    }
                    is Lce.Content -> {
                        UtilMethods.hideLoading()
                        showToast("Otp sent")
                    }
                    is Lce.Error -> {
                        UtilMethods.hideLoading()
                        showAlertDialog("", it.error)
                    }
                }
            })
    }
}