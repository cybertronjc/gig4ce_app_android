package com.gigforce.verification.mainverification.vaccine.bottomsheets

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.verification.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class GetVaccinateFirstBS : BottomSheetDialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.get_vaccinate_first_bs, container, false)
    }



}