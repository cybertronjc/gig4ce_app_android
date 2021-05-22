package com.gigforce.landing_screen.landingscreen.explorebyrole.jdscreens

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.j_d_screen_fragment.*

class JDScreenFragment : BaseFragment() {

    companion object {
        fun newInstance() = JDScreenFragment()
    }

    private lateinit var viewModel: JDScreenViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.j_d_screen_fragment,inflater,container)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(JDScreenViewModel::class.java)
        back_button.setOnClickListener{
            activity?.onBackPressed()
        }
    }

}