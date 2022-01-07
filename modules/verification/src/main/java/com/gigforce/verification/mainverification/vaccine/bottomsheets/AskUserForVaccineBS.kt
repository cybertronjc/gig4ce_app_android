package com.gigforce.verification.mainverification.vaccine.bottomsheets

import android.os.Bundle

import android.view.LayoutInflater

import android.view.View

import android.view.ViewGroup
import androidx.fragment.app.DialogFragment

import androidx.fragment.app.viewModels

import androidx.lifecycle.Observer

import com.gigforce.core.utils.Lce

import com.gigforce.verification.R
import com.gigforce.verification.mainverification.vaccine.VaccineViewModel

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint

import kotlinx.android.synthetic.main.ask_user_for_vaccine_bs.*
@AndroidEntryPoint
class AskUserForVaccineBS : BottomSheetDialogFragment() {
    private val vaccineViewModel : VaccineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BSDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.ask_user_for_vaccine_bs, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
                super.onViewCreated(view, savedInstanceState)
                vaccineViewModel.vaccineConfigLiveData.observe(viewLifecycleOwner, Observer{
                        when(it){
                                Lce.Loading->{}
                                is Lce.Content -> {
                                        if(it.content.list.isNullOrEmpty()){
                                                dismiss()
                                            }else{
                                                vaccinerv.collection = it.content?.list?: emptyList()
                                            }
                                    }
                                is Lce.Error -> {
                                        dismiss()
                                    }
                            }
                    })
            }

}