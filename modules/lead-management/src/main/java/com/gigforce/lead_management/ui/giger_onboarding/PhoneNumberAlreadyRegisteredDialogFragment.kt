package com.gigforce.lead_management.ui.giger_onboarding

import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.gigforce.core.base.BaseDialogFragment
import com.gigforce.lead_management.R
import com.gigforce.lead_management.databinding.FragmentNumberAlreadyRegisteredBinding
import dagger.hilt.android.AndroidEntryPoint

class PhoneNumberAlreadyRegisteredDialogFragment :
    BaseDialogFragment<FragmentNumberAlreadyRegisteredBinding>(
        fragmentName = "PhoneNumberAlreadyRegisteredDialogFragment",
        layoutId = R.layout.fragment_number_already_registered,
    ) {

    companion object {
        const val TAG = "PhoneNumberAlreadyRegisteredDialogFragment"

        fun launch(
            fragmentManager: FragmentManager
        ) {

            val dialog = PhoneNumberAlreadyRegisteredDialogFragment()
            try {
                dialog.show(fragmentManager, TAG)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun viewCreated(
        viewBinding: FragmentNumberAlreadyRegisteredBinding,
        savedInstanceState: Bundle?
    ) {

        viewBinding.okayBtn.setOnClickListener {
            dismiss()
        }
    }
}