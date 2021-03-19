package com.gigforce.verification.gigerVerfication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import com.gigforce.verification.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_select_image_source.*

interface SelectImageSourceBottomSheetActionListener {
    fun onImageSourceSelected(source: ImageSource)
}

enum class ImageSource {
    CAMERA,
    GALLERY
}

class SelectImageSourceBottomSheet : BottomSheetDialogFragment() {

    private lateinit var selectImageSourceBottomSheetActionListener: SelectImageSourceBottomSheetActionListener

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_select_image_source, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {
        cameraLabel.setOnClickListener {
            selectImageSourceBottomSheetActionListener.onImageSourceSelected(ImageSource.CAMERA)
            dismiss()
        }

        galleryLabel.setOnClickListener {
            selectImageSourceBottomSheetActionListener.onImageSourceSelected(ImageSource.GALLERY)
            dismiss()
        }
    }

    companion object {
        const val TAG = "SelectImageSourceBottomSheet"

        fun launch(
            childFragmentManager: FragmentManager,
            selectImageSourceBottomSheetActionListener: SelectImageSourceBottomSheetActionListener
        ) = SelectImageSourceBottomSheet()
            .apply {
                this.selectImageSourceBottomSheetActionListener =
                    selectImageSourceBottomSheetActionListener
            }
            .show(childFragmentManager, TAG)
    }
}