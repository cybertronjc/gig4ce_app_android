package com.gigforce.app.modules.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.gigforce.app.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.layout_verification_done.view.*

class VerificationDone:BottomSheetDialogFragment() {

    companion object {
        fun newInstance() = Verification()
    }

    lateinit var layout: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.layout_verification_done, container, false);
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.buttonVeriDone.setOnClickListener { findNavController().navigate(R.id.mainHomeScreen) }
    }
}