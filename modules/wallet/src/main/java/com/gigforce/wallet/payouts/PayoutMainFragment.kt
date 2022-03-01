package com.gigforce.wallet.payouts

import android.os.Bundle
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.gigforce.common_ui.CommonIntentExtras
import com.gigforce.common_ui.DisplayUtil.px
import com.gigforce.core.base.BaseFragment2
import com.gigforce.wallet.R
import com.gigforce.wallet.databinding.FragmentPayoutMainBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PayoutMainFragment : BaseFragment2<FragmentPayoutMainBinding>(
    fragmentName = TAG,
    layoutId = R.layout.fragment_payout_main,
    statusBarColor = R.color.lipstick_2
) {
    companion object {
        const val TAG = "PayoutMainFragment"
    }

    private var title = ""
    private val sharedViewModel: SharedPayoutViewModel by activityViewModels()

    override fun shouldPreventViewRecreationOnNavigation(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getIntentData(savedInstanceState)
    }

    private fun getIntentData(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            title = it.getString(CommonIntentExtras.INTENT_EXTRA_TOOLBAR_TITLE) ?: ""
        } ?: run {
            arguments?.let {
                title = it.getString(CommonIntentExtras.INTENT_EXTRA_TOOLBAR_TITLE) ?: ""
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(CommonIntentExtras.INTENT_EXTRA_TOOLBAR_TITLE, title)
    }

    override fun viewCreated(
        viewBinding: FragmentPayoutMainBinding,
        savedInstanceState: Bundle?
    ) {
        initAppBar()
        viewBinding.viewPager.adapter = PayoutMainViewPagerAdapter(this)
        viewBinding.viewPager.registerOnPageChangeCallback(viewPagePageChangeCallback)

        TabLayoutMediator(
            viewBinding.tablayout,
            viewBinding.viewPager
        ) { tab, position ->
            tab.text = PayoutMainViewPagerAdapter.TABS[position].fragmentTabName
        }.attach()
        initTabLayout()
    }

    private fun initTabLayout() {
        val tabs = viewBinding.tablayout.getChildAt(0) as ViewGroup

        for (i in 0 until tabs.childCount) {
            val tab = tabs.getChildAt(i)
            val layoutParams = tab.layoutParams as LinearLayout.LayoutParams
            layoutParams.weight = 0f

            layoutParams.marginEnd = 12.px
            tab.layoutParams = layoutParams
            viewBinding.tablayout.requestLayout()
        }

        viewBinding.viewPager.registerOnPageChangeCallback(viewPagePageChangeCallback)
    }

    private fun initAppBar() = viewBinding.appBarComp.apply {
        if (title.isNotBlank())
            setAppBarTitle(title)
        else
            setAppBarTitle("Payouts")
        changeBackButtonDrawable()
        makeBackgroundMoreRound()
        setBackButtonListener {
            findNavController().navigateUp()
        }

        filterImageButton.setOnClickListener {
            sharedViewModel.openPayoutFilter()
        }
    }

    private val viewPagePageChangeCallback = object : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)

            try {
                viewBinding.appBarComp.filterImageButton.isVisible = position == 0
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        try {
            viewBinding.viewPager.unregisterOnPageChangeCallback(viewPagePageChangeCallback)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDestroy()
    }
}