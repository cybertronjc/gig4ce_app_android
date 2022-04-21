package com.gigforce.verification.mainverification

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.common_ui.CommonIntentExtras
import com.gigforce.common_ui.DisplayUtil.px
import com.gigforce.core.base.BaseFragment2
import com.gigforce.core.navigation.INavigation
import com.gigforce.verification.R
import com.gigforce.verification.databinding.FragmentMyDocumentsBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MyDocumentsFragment : BaseFragment2<FragmentMyDocumentsBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_my_documents,
    statusBarColor = R.color.lipstick_2
) {

    companion object {
        fun newInstance() = MyDocumentsFragment()
        const val TAG = "MyDocumentsFragment"
    }

    @Inject
    lateinit var navigation: INavigation

    lateinit var pagerAdapter: MyDocumentsPagerAdapter

    private var title = ""
    var selectedTab = 0
    var deepLinkTab: Int? = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentData(savedInstanceState)
    }

    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString(CommonIntentExtras.INTENT_EXTRA_TOOLBAR_TITLE) ?: ""
            deepLinkTab = it.getInt(CommonIntentExtras.INTENT_EXTRA_SELECTED_TAB)
        } ?: run {
            arguments?.let {
                title = it.getString(CommonIntentExtras.INTENT_EXTRA_TOOLBAR_TITLE) ?: ""
                deepLinkTab = it.getInt(CommonIntentExtras.INTENT_EXTRA_SELECTED_TAB)
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CommonIntentExtras.INTENT_EXTRA_TOOLBAR_TITLE, title)
    }

    override fun viewCreated(
        viewBinding: FragmentMyDocumentsBinding,
        savedInstanceState: Bundle?
    ) {
        initAppBar()
        viewBinding.viewPager.adapter = MyDocumentsPagerAdapter(this)
        //viewBinding.viewPager.registerOnPageChangeCallback(viewPagePageChangeCallback)

        TabLayoutMediator(
            viewBinding.verificationTabLayout,
            viewBinding.viewPager
        ) { tab, position ->
            tab.text = MyDocumentsPagerAdapter.TABS[position].fragmentTabName
        }.attach()
        initTabLayout()
    }

    private fun initTabLayout() {
        val tabs = viewBinding.verificationTabLayout.getChildAt(0) as ViewGroup

        for (i in 0 until tabs.childCount) {
            val tab = tabs.getChildAt(i)
            val layoutParams = tab.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 0f

            layoutParams.marginEnd = 12.px
            tab.layoutParams = layoutParams
            viewBinding.verificationTabLayout.requestLayout()
        }

        viewBinding.verificationTabLayout.selectTab(deepLinkTab?.let {
            viewBinding.verificationTabLayout.getTabAt(
                it
            )
        })

        //viewBinding.viewPager.registerOnPageChangeCallback(viewPagePageChangeCallback)
    }

    private fun initAppBar() = viewBinding.appBarComp.apply {
        if (title.isNotBlank())
            setAppBarTitle(title)
        else
            setAppBarTitle("My Documents")
        changeBackButtonDrawable()
        makeBackgroundMoreRound()
        setBackButtonListener {
            findNavController().navigateUp()
        }
    }

//    private val viewPagePageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
//
//        override fun onPageSelected(position: Int) {
//            super.onPageSelected(position)
//
//            try {
//                viewBinding.appBarComp.filterImageButton.isVisible = position == 0
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        try {
//            viewBinding.viewPager.unregisterOnPageChangeCallback(viewPagePageChangeCallback)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }
//        super.onDestroy()
//    }


}