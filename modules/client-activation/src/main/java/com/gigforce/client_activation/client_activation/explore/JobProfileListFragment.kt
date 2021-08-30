package com.gigforce.client_activation.client_activation.explore

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.client_activation.R

class JobProfileListFragment : Fragment() {

    companion object {
        fun newInstance() = JobProfileListFragment()
    }

    private lateinit var viewModel: JobProfileListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.job_profile_list_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(JobProfileListViewModel::class.java)
        // TODO: Use the ViewModel
    }

}