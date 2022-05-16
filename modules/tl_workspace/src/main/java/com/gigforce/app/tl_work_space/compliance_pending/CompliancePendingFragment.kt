package com.gigforce.app.tl_work_space.compliance_pending

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.tl_work_space.R

class CompliancePendingFragment : Fragment() {

    companion object {
        fun newInstance() = CompliancePendingFragment()
    }

    private lateinit var viewModel: CompliancePendingViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_compliance_pending, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CompliancePendingViewModel::class.java)
        // TODO: Use the ViewModel
    }

}