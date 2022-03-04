package com.gigforce.verification.mainverification

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.res.ResourcesCompat
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.FragmentMyDocumentsBinding
import com.gigforce.verification.mainverification.compliance.ComplianceDocsFragment
import com.google.android.material.tabs.TabLayoutMediator
import com.jaeger.library.StatusBarUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.verification_main_fragment.*
import java.lang.NullPointerException
import javax.inject.Inject

@AndroidEntryPoint
class MyDocumentsFragment : Fragment() {

    companion object {
        fun newInstance() = MyDocumentsFragment()
        const val TAG = "MyDocumentsFragment"
    }

    @Inject
    lateinit var navigation: INavigation

    lateinit var pagerAdapter: MyDocumentsPagerAdapter

    var selectedTab = 0
    private lateinit var viewBinding: FragmentMyDocumentsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        StatusBarUtil.setColorNoTranslucent(
            requireActivity(),
            ResourcesCompat.getColor(resources, R.color.lipstick_2, null)
        )
        viewBinding = FragmentMyDocumentsBinding.inflate(inflater, container, false)
        return viewBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getIntentData(savedInstanceState)
        initViews()
        initListeners()
    }

    var title = ""
    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString("title") ?: ""
        } ?: run {
            arguments?.let {
                title = it.getString("title") ?: ""
            }
        }
    }

    private fun initListeners() = viewBinding.apply{
        val mediaArray = arrayOf(
            "KYC Docs",
            "Compliance"
        )
        pagerAdapter = MyDocumentsPagerAdapter(childFragmentManager, lifecycle)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(verificationTabLayout, viewPager) { tab, position ->
            tab.text = mediaArray[position]
        }.attach()
    }

    private fun initViews() = viewBinding.apply{

        appBarComp.apply {
            if (title.isNotBlank())
                this.setAppBarTitle(title)
        }

        val betweenSpace = 25

        val slidingTabStrip: ViewGroup = verificationTabLayout.getChildAt(0) as ViewGroup

        for (i in 0 until slidingTabStrip.childCount - 1) {
            val v: View = slidingTabStrip.getChildAt(i)
            val params: ViewGroup.MarginLayoutParams =
                v.layoutParams as ViewGroup.MarginLayoutParams
            params.rightMargin = betweenSpace
        }

        try {
            //showToast("position: ${selectedTab}")
            verificationTabLayout.getTabAt(selectedTab)?.select()
        } catch (e: NullPointerException) {
            e.printStackTrace()
        }

    }
}