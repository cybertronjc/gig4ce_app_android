package com.gigforce.common_ui.utils

import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.common_ui.R
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_bs_location_access_dialog.*

class BsBackgroundAndLocationAccess : BottomSheetDialogFragment() {

    private var onLocationOkayButtonPressClickListener: OnLocationOkayButtonPressClickListener? =
        null

    fun setOnLocationOkayClickListener(
        onLocationOkayButtonPressClickListener: OnLocationOkayButtonPressClickListener
    ) {
        this.onLocationOkayButtonPressClickListener = onLocationOkayButtonPressClickListener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.setOnShowListener { dialog ->
            val d = dialog as BottomSheetDialog
            val bottomSheetInternal =
                d.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            BottomSheetBehavior.from<View?>(bottomSheetInternal!!)
                .setState(BottomSheetBehavior.STATE_EXPANDED)
        }
        return inflater.inflate(R.layout.layout_bs_location_access_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            description_bg_service.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }
        tv_allow_access.setOnClickListener {
            onLocationOkayButtonPressClickListener?.onRequestLocationPermissionButtonClicked()
            dismiss()
        }
    }

    interface OnLocationOkayButtonPressClickListener {

        fun onRequestLocationPermissionButtonClicked()
    }
}