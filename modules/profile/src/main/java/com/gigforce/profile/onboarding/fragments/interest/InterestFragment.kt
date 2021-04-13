package com.gigforce.profile.onboarding.fragments.interest

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.profile.R

class InterestFragment : Fragment() {

    companion object {
        fun newInstance() = InterestFragment()
    }

    private lateinit var viewModel: InterestViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.interest_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(InterestViewModel::class.java)
        // TODO: Use the ViewModel
    }

}