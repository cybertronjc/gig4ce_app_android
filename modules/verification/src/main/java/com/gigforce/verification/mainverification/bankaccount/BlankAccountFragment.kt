package com.gigforce.verification.mainverification.bankaccount

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.verification.R

class BlankAccountFragment : Fragment() {

    companion object {
        fun newInstance() = BlankAccountFragment()
    }

    private lateinit var viewModel: BlankAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.blank_account_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BlankAccountViewModel::class.java)
        // TODO: Use the ViewModel
    }

}