package com.gigforce.app.modules.verification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.gigforce.app.R
import com.gigforce.app.modules.auth.ui.main.LoginViewModel
import com.gigforce.app.modules.auth.ui.main.MobileInput
import kotlinx.android.synthetic.main.mobile_number_input.view.*

class Verification: Fragment() {
    companion object {
        fun newInstance() = MobileInput()
    }

    lateinit var layout: View
    lateinit var viewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        layout = inflater.inflate(R.layout.mobile_number_input, container, false)
        return layout
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.activity = this.activity!!
    }
}