package com.gigforce.app.modules.learning.slides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.viewpager.widget.ViewPager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.fragment_slides.*


class SlidesFragment : BaseFragment(), ViewPager.OnPageChangeListener {

    private val viewModel: SlideViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_slides, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    private fun initView() {

        slideCounterTV.text = "Slide 1 of ${viewModel.slidesData.size}"
        slideTitleTV.text = viewModel.slidesData[0].title
        slideDescriptionTV.text = viewModel.slidesData[0].content

        val imageList = viewModel.slidesData.map { it.image }

        val pagerAdapter = SlidesPagerAdapter(childFragmentManager,imageList)
        singleImageSlideViewpager.adapter = pagerAdapter
        singleImageSlideViewpager.addOnPageChangeListener(this)
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        val data = viewModel.slidesData[position]

        slideCounterTV.text = "Slide ${position + 1} of ${viewModel.slidesData.size}"
        slideTitleTV.text = data.title
        slideDescriptionTV.text = data.content
    }

}