package com.gigforce.app.modules.ambassador_user_enrollment.user_rollment.profilePicture

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.gigforce.app.R
import com.gigforce.app.utils.StringConstants
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_click_or_select_profile_picture.*


class ClickOrSelectImageBottomSheet : BottomSheetDialogFragment() {

    private var isProfilePicPresent: Boolean = false
    private var listener: OnPickOrCaptureImageClickListener? = null

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(StringConstants.PROFILE_PIC_PRESENT.value, isProfilePicPresent)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            isProfilePicPresent =
                it.getBoolean(StringConstants.PROFILE_PIC_PRESENT.value, false)

        }

        arguments?.let {
            isProfilePicPresent =
                it.getBoolean(StringConstants.PROFILE_PIC_PRESENT.value, false)

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_click_or_select_profile_picture, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initView()
    }


    private fun initView() {
        remove_profile_pic.visibility = if (isProfilePicPresent) View.VISIBLE else View.GONE
        camera_layout.setOnClickListener {
            listener?.onClickPictureThroughCameraClicked()
            dismiss()
        }
        gallery_layout.setOnClickListener {
            listener?.onPickImageThroughCameraClicked()
            dismiss()
        }
        remove_profile_pic.setOnClickListener {
            listener?.removeProfilePic()
            dismiss()
        }
    }

    companion object {
        const val TAG = "ClickOrSelectImageBottomSheet"

        fun launch(
            childFragmentManager: FragmentManager,
            isPicturePresent: Boolean,
            listener: OnPickOrCaptureImageClickListener
        ) {

            val fragment = ClickOrSelectImageBottomSheet()
            fragment.arguments =
                bundleOf(StringConstants.PROFILE_PIC_PRESENT.value to isPicturePresent)
            fragment.listener = listener
            fragment.show(childFragmentManager, TAG)
        }
    }

    interface OnPickOrCaptureImageClickListener {

        fun onClickPictureThroughCameraClicked()

        fun onPickImageThroughCameraClicked()

        fun removeProfilePic()
    }
}