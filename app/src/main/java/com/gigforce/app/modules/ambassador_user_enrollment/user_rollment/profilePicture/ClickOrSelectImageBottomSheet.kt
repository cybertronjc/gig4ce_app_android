package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.profilePicture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_click_or_select_profile_picture.*


class ClickOrSelectImageBottomSheet : BottomSheetDialogFragment() {

    private var listener: OnPickOrCaptureImageClickListener? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_click_or_select_profile_picture, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }


    private fun initView() {
        camera_layout.setOnClickListener {
            listener?.onClickPictureThroughCameraClicked()
            dismiss()
        }
        gallery_layout.setOnClickListener {
            listener?.onPickImageThroughCameraClicked()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ClickOrSelectImageBottomSheet"

        fun launch(
            childFragmentManager: FragmentManager,
            listener: OnPickOrCaptureImageClickListener
        ) {

            val fragment = ClickOrSelectImageBottomSheet()
            fragment.listener = listener
            fragment.show(childFragmentManager, TAG)
        }
    }

    interface OnPickOrCaptureImageClickListener {

        fun onClickPictureThroughCameraClicked()

        fun onPickImageThroughCameraClicked()
    }
}