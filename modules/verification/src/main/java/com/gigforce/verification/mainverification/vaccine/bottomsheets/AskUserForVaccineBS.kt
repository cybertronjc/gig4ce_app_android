package com.gigforce.verification.mainverification.vaccine.bottomsheets

import android.os.Bundle

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup

import androidx.fragment.app.viewModels

import androidx.lifecycle.Observer

import com.gigforce.core.utils.Lce

import com.gigforce.verification.R
import com.gigforce.verification.mainverification.vaccine.VaccineViewModel

import com.google.android.material.bottomsheet.BottomSheetDialogFragment

import kotlinx.android.synthetic.main.ask_user_for_vaccine_bs.*

class AskUserForVaccineBS : BottomSheetDialogFragment() {
    val vaccineViewModel : VaccineViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.ask_user_for_vaccine_bs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

                super.onViewCreated(view, savedInstanceState)

                vaccineViewModel.vaccineConfigLiveData.observe(viewLifecycleOwner, Observer{

                        when(it){

                                Lce.Loading->{}

                                is Lce.Content -> {

                                        if(it.content.list.isEmpty()){

                                                dismiss()

                                            }else{

                                                vaccinerv.collection = it.content.list

                                            }

                                    }

                                is Lce.Error -> {

                                        dismiss()

                                    }



                            }

                    })

            }

}