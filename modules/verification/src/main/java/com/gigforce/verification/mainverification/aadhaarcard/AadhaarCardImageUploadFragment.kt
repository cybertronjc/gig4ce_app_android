package com.gigforce.verification.mainverification.aadhaarcard

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.gigforce.common_ui.ext.showToast
import com.gigforce.verification.R
import com.gigforce.verification.databinding.AadhaarCardImageUploadFragmentBinding

class AadhaarCardImageUploadFragment : Fragment() {

    companion object {
        fun newInstance() = AadhaarCardImageUploadFragment()
    }

    private val viewModel: AadhaarCardImageUploadViewModel by viewModels()
    private lateinit var viewBinding : AadhaarCardImageUploadFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = AadhaarCardImageUploadFragmentBinding.inflate(inflater,container,false)
        return viewBinding.root
//        return inflater.inflate(R.layout.aadhaar_card_image_upload_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        viewBinding.overview.setOnClickListener{
//            showToast("working")
//        }
    }

}