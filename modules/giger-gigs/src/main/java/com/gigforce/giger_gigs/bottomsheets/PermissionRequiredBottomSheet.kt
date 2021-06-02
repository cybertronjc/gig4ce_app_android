package com.gigforce.giger_gigs.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.core.os.bundleOf
import androidx.core.text.buildSpannedString
import androidx.fragment.app.FragmentManager
import com.gigforce.giger_gigs.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.util.*


class PermissionRequiredBottomSheet : BottomSheetDialogFragment() {

    /**
     * Permission that will be asked on clicking yes
     */
    private lateinit var permissionList: HashMap<String, String>
    private var permissionLabelText: String? = null
    private var permissionBottomSheetActionListener: PermissionBottomSheetActionListener? = null

    @DrawableRes
    private var imageToShow: Int? = null

    //View
    private lateinit var permissionImageView: ImageView
    private lateinit var permissionLabelTextView: TextView
    private lateinit var permissionTextView: TextView
    private lateinit var permissionOkayButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_permission_required, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getData(arguments, savedInstanceState)
        initView(view)
        setDataOnView()
    }


    private fun getData(arguments: Bundle?, savedInstanceState: Bundle?) {
        arguments?.let {

            permissionList = it.getSerializable(INTENT_PERMISSION_LIST) as HashMap<String, String>
            permissionLabelText = it.getString(INTENT_PERMISSION_TEXT)
            imageToShow = if (it.getInt(INTENT_PERMISSION_IMAGE) != -1) {
                it.getInt(INTENT_PERMISSION_IMAGE)
            } else {
                null
            }
        }

        savedInstanceState?.let {

            permissionList = it.getSerializable(INTENT_PERMISSION_LIST) as HashMap<String, String>
            permissionLabelText = it.getString(INTENT_PERMISSION_TEXT)
            imageToShow = if (it.getInt(INTENT_PERMISSION_IMAGE) != -1) {
                it.getInt(INTENT_PERMISSION_IMAGE)
            } else {
                null
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(INTENT_PERMISSION_LIST, permissionList)
        outState.putInt(INTENT_PERMISSION_IMAGE, imageToShow ?: -1)
        outState.putString(INTENT_PERMISSION_TEXT, permissionLabelText)
    }

    private fun initView(view: View) {

        permissionImageView = view.findViewById(R.id.permission_image_view)
        permissionLabelTextView = view.findViewById(R.id.permission_label_text_view)
        permissionTextView = view.findViewById(R.id.permission_list_textview)
        permissionOkayButton = view.findViewById(R.id.permission_okay_button)

        permissionOkayButton.setOnClickListener {
            permissionBottomSheetActionListener?.onPermissionOkayClicked()
            dismiss()
        }
    }

    private fun setDataOnView() {

        val imageToShow = imageToShow ?: R.drawable.ic_location_permission
        val permissionLabelText =
            permissionLabelText ?: "Following permission are required to proceed further"

        val permissionAndReasonText = buildSpannedString {
            permissionList.forEach {

                append(it.key)
                append(" - ")
                append(it.value)
                append("\n")
            }
        }

//        Glide.with(requireContext())
//            .load(imageToShow)
//            .into(permissionImageView)

        permissionLabelTextView.text = permissionLabelText
        permissionTextView.text = permissionAndReasonText
    }

    interface PermissionBottomSheetActionListener {

        fun onPermissionOkayClicked()
    }

    companion object {
        private const val INTENT_PERMISSION_LIST = "permission_list"
        private const val INTENT_PERMISSION_TEXT = "permission_text"
        private const val INTENT_PERMISSION_IMAGE = "permission_image"

        const val TAG = "PermissionRequiredBottomSheet"

        fun launch(
                childFragmentManager: FragmentManager,
                permissionBottomSheetActionListener: PermissionBottomSheetActionListener,
                permissionWithReason: Map<String, String>,
                permissionText: String? = null,
                @DrawableRes imageToShow: Int? = null
        ) {
            PermissionRequiredBottomSheet().apply {
                this.permissionBottomSheetActionListener = permissionBottomSheetActionListener
                arguments = bundleOf(
                    INTENT_PERMISSION_LIST to permissionWithReason,
                    INTENT_PERMISSION_IMAGE to imageToShow,
                    INTENT_PERMISSION_TEXT to permissionText
                )
                show(childFragmentManager, TAG)
            }
        }
    }
}