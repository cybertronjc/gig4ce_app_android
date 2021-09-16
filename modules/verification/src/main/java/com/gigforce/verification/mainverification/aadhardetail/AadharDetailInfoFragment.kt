package com.gigforce.verification.mainverification.aadhardetail

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.verification.R

class AadharDetailInfoFragment : Fragment() {

    companion object {
        fun newInstance() = AadharDetailInfoFragment()
    }

    private lateinit var viewModel: AadharDetailInfoViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.aadhar_detail_info_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AadharDetailInfoViewModel::class.java)
        // TODO: Use the ViewModel
    }

}