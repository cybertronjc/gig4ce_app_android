package com.gigforce.app.modules.profile

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.LayoutInflater
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class EducationFormBSFragment: BottomSheetDialogFragment() {

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = LayoutInflater.from(context).inflate(R.layout.form_education, null)
        dialog.setContentView(view)
    }
}