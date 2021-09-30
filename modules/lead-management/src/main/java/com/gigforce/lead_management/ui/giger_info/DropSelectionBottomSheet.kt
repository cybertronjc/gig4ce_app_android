package com.gigforce.lead_management.ui.giger_info

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.gigforce.common_ui.StringConstants
import com.gigforce.lead_management.R


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class DropSelectionBottomSheet : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drop_selection_bottom_sheet, container, false)
    }

    companion object {
        const val TAG = "DropSelectionBottomSheet"
        fun launch(
        ) {

        }
    }
}