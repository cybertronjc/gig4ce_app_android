package com.gigforce.verification.mainverification.vaccine.mainvaccine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.gigforce.core.navigation.INavigation
import com.gigforce.core.utils.GlideApp
import com.gigforce.verification.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.certificate_download_bs.*
import javax.inject.Inject

@AndroidEntryPoint
class CertificateDownloadBS : BottomSheetDialogFragment() {

    var title: String? = null

    @Inject
    lateinit var navigation : INavigation
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NORMAL, R.style.BSDialogStyle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.certificate_download_bs, container, false)
    }


    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString("title") ?: ""
        } ?: run {
            arguments?.let {
                title = it.getString("title") ?: ""
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData(savedInstanceState)
        GlideApp.with(this).load(R.drawable.ok_downloaded).into(imageView2)
        okay_bn_bs.setOnClickListener{
            dismiss()
        }
        initViews()
    }

    private fun initViews() {
        if (title?.isNotEmpty() == true){
            textView14.text = title
        }
    }

}