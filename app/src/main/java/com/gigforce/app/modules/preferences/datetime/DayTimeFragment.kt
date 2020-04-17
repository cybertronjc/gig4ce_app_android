package com.gigforce.app.modules.preferences.datetime

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.date_time_fragment.*

class DayTimeFragment : BaseFragment() {

    companion object {
        fun newInstance() = DayTimeFragment()
    }

    private lateinit var viewModel: DayTimeViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.date_time_fragment, inflater,container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(DayTimeViewModel::class.java)
        listener()
    }

    private fun listener() {
        textView49.setOnClickListener(View.OnClickListener { navigate(R.id.weekDayFragment) })
        textView55.setOnClickListener(View.OnClickListener {  })
    }

}