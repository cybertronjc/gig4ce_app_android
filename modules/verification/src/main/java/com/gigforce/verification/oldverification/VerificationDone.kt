package com.gigforce.verification.oldverification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.layout_verification_done.view.*
import javax.inject.Inject

@AndroidEntryPoint
class VerificationDone : BottomSheetDialogFragment() {

    companion object {
        fun newInstance() =
            Verification()
    }

    lateinit var layout: View
    @Inject lateinit var navigation : INavigation
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        layout = inflater.inflate(R.layout.layout_verification_done, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout.buttonVeriDone.setOnClickListener {
            navigation.navigateTo("main_home_screen")
//            findNavController().navigate(R.id.mainHomeScreen)
        }
    }
}