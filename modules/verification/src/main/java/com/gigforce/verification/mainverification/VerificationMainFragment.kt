package com.gigforce.verification.mainverification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.viewdatamodels.SimpleCardDVM
import com.gigforce.core.extensions.gone
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.util.VerificationConstants
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.verification_main_fragment.*
import javax.inject.Inject


@AndroidEntryPoint
class VerificationMainFragment : Fragment() {

    companion object {
        fun newInstance() = VerificationMainFragment()
    }

    @Inject
    lateinit var navigation: INavigation
    private val viewModel: VerificationMainViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.verification_main_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()
        listener()
    }

    private fun listener() {

        next.setOnClickListener {

            allDocsRV.collection?.let {
                (it as ArrayList<SimpleCardDVM>).filter { doc -> doc.isSelected }.let {

                    if(it.isNullOrEmpty()){
                     showToast("Please select minimum one document to upload")
                    }else {
                        var navigationsForBundle = emptyList<String>()
                        if (it.size > 1) {
                            navigationsForBundle = it.slice(IntRange(1, it.size - 1)).map { it.navpath }
                        }
                        navigation.navigateTo(it.get(0).navpath, bundleOf(VerificationConstants.NAVIGATION_STRINGS to navigationsForBundle))
                    }
                }
            }

        }

        appBar2.setBackButtonListener(View.OnClickListener {
            activity?.onBackPressed()
        })

    }

    private fun observer() {
        viewModel.allDocumentsData.observe(viewLifecycleOwner, Observer {
            allDocsRV.collection = it
        })

        viewModel.allDocumentsVerified.observe(viewLifecycleOwner, Observer {
            if (it) {
                textView7.gone()
            }

        })
    }

    override fun onResume() {
        super.onResume()
        StatusBarUtil.setColorNoTranslucent(requireActivity(), ResourcesCompat.getColor(resources, R.color.lipstick_2, null))
    }
}
