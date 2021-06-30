package com.gigforce.verification.mainverification.bankaccount

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.verification.R

class BankAccountFragment : Fragment() {

    companion object {
        fun newInstance() = BankAccountFragment()
    }

    private lateinit var viewModel: BankAccountViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bank_account_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(BankAccountViewModel::class.java)
        // TODO: Use the ViewModel
    }

}