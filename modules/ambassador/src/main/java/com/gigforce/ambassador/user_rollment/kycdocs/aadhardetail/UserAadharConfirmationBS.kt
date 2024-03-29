package com.gigforce.ambassador.user_rollment.kycdocs.aadhardetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.gigforce.ambassador.R
import com.gigforce.common_ui.ext.pushOnclickListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.user_aadhar_confirmation_bs_layout.*

@AndroidEntryPoint
class UserAadharConfirmationBS : BottomSheetDialogFragment() {

    private val viewModel: UserAadharDetailInfoViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BSDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.user_aadhar_confirmation_bs_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listener()
    }

    private fun listener() {

        cancel_bn_bs.pushOnclickListener {
            dismiss()
        }

        okay_bn_bs.pushOnclickListener {
            viewModel.confirmDOBEdit()
            dismiss()
        }
    }
}