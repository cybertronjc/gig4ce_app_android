package com.gigforce.lead_management.ui.new_selection_form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.gigforce.common_ui.ext.pushOnclickListener
import com.gigforce.core.navigation.INavigation
import com.gigforce.lead_management.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class UserAlreadyExistsBottomSheet : BottomSheetDialogFragment() {

    @Inject
    lateinit var navigation: INavigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BSDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_user_already_exists_bs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData()
        findViews(view)
        initializeAll()
        listeners()
    }

    private fun findViews(view: View) {

    }

    private fun listeners() {
        requireView().findViewById<View>(R.id.okay_btn).pushOnclickListener{
            dismiss()
        }
    }

    private fun initializeAll() {
        if (errorMessage.isNotBlank()) {
            requireView().findViewById<TextView>(R.id.subheading).text = errorMessage
        }
    }

    var errorMessage = ""
    private fun getIntentData() {
        arguments?.let {
            errorMessage = it.getString("message") ?: ""
        }
    }

}