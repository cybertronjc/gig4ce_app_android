package com.gigforce.app.modules.preferences.daytime.weekend

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R

class WeekEndFragment : Fragment() {

    companion object {
        fun newInstance() = WeekEndFragment()
    }

    private lateinit var viewModel: WeekEndViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.week_end_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(WeekEndViewModel::class.java)
        // TODO: Use the ViewModel
    }

}