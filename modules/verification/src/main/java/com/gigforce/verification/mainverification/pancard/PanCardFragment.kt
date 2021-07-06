package com.gigforce.verification.mainverification.pancard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.viewdatamodels.KYCImageModel
import com.gigforce.verification.R
import com.gigforce.verification.databinding.PanCardFragmentBinding

class PanCardFragment : Fragment() {

    companion object {
        fun newInstance() = PanCardFragment()
    }

    private val viewModel: PanCardViewModel by viewModels()
    private lateinit var viewBinding: PanCardFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = PanCardFragmentBinding.inflate(inflater, container, false)
        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
        observer()

    }

    private fun observer() {

    }

    private fun setViews() {
        val list = listOf(KYCImageModel("Pan Card", R.drawable.ic_front))
        viewBinding.toplayoutblock.setImageViewPager(list)
    }
}