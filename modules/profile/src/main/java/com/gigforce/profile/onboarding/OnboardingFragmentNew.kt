package com.gigforce.profile.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.gigforce.profile.R
import com.gigforce.profile.onboarding.adapter.MultiviewsAdapter
import com.gigforce.profile.onboarding.adapter.MutlifragmentAdapter
import kotlinx.android.synthetic.main.onboarding_fragment_new_fragment.*


class OnboardingFragmentNew : Fragment() {

    companion object {
        fun newInstance() = OnboardingFragmentNew()
    }

    private lateinit var viewModel: OnboardingFragmentNewViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.onboarding_fragment_new_fragment, container, false)
    }
    var adapter : MultiviewsAdapter? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
                viewModel = ViewModelProvider(this).get(OnboardingFragmentNewViewModel::class.java)
        disableViewPagerScroll()
//        context?.let {
//
//            adapter = MultiviewsAdapter(it,viewModel.getOnboardingData())
//
//            onboarding_rv.layoutManager = LinearLayoutManager(
//                activity?.applicationContext,
//                LinearLayoutManager.HORIZONTAL,
//                false
//            )
//
//            onboarding_rv.adapter = adapter
//            var pagerHelper = PagerSnapHelper()
//            pagerHelper.attachToRecyclerView(onboarding_rv)
//
//        }

        activity?.let {
            onboarding_rv.adapter = MutlifragmentAdapter(it)

        }
        next.setOnClickListener(View.OnClickListener {
//            adapter?.let {
//                val layoutManager =
//                    onboarding_rv.getLayoutManager() as LinearLayoutManager
//                val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
//                it.validateScreen(firstVisiblePosition)
//                if(it.itemCount>firstVisiblePosition+1) {
//                    onboarding_rv.smoothScrollToPosition(firstVisiblePosition + 1)
//                }
////                (onboarding_rv.layoutManager as LinearLayoutManager)?.scrollToPositionWithOffset(
////                    firstVisiblePosition,
////                    0
////                )
//            }

            onboarding_rv.setCurrentItem(onboarding_rv.currentItem+1)
        })


    }

    private fun disableViewPagerScroll() {
        onboarding_rv.isUserInputEnabled = false
    }

    //--------------


}