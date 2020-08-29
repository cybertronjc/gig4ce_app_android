package com.gigforce.app.modules.learning.slides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.data.SlideContent
import com.gigforce.app.utils.Lce
import kotlinx.android.synthetic.main.fragment_slides.*
import kotlinx.android.synthetic.main.fragment_slides_main.*


class SlidesFragment : BaseFragment(), ViewPager.OnPageChangeListener {

    private val viewModel: SlideViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_slides, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        initViewModel()
    }

    private fun initViewModel() {

        viewModel.slideContent
            .observe(viewLifecycleOwner, Observer {

                when (it) {
                    Lce.Loading -> showSlidesLoadingLayout()
                    is Lce.Content -> showSlides(it.content)
                    is Lce.Error -> showErrorInLoadingSlidesLayout(it.error)
                }
            })

        viewModel.getSlideContent(
            courseId = "",
            moduleId = "",
            lessonId = ""
        )
    }

    private fun showErrorInLoadingSlidesLayout(error: String) {
        fragment_slides_main_layout.gone()
        fragment_slides_progress_bar.gone()
        fragment_slides_error.visible()
        fragment_slides_error.text = error
    }

    private var pagerAdapter: SlidesPagerAdapter? = null

    private fun showSlides(content: List<SlideContent>) {

        fragment_slides_progress_bar.gone()
        fragment_slides_error.gone()
        fragment_slides_main_layout.visible()

        pagerAdapter = SlidesPagerAdapter(childFragmentManager, content)
        slideViewPager.adapter = pagerAdapter
        slideViewPager.addOnPageChangeListener(this)

        toolbar.subtitle = "1 of ${content.size}"
        val slidesCoverageProgress =  100 / content.size
        progress.progress = slidesCoverageProgress
    }

    private fun showSlidesLoadingLayout() {

        fragment_slides_main_layout.gone()
        fragment_slides_error.gone()
        fragment_slides_progress_bar.visible()
    }

    private fun initView() {

//        slideCounterTV.text = "Slide 1 of ${viewModel.slidesData.size}"
//        slideTitleTV.text = viewModel.slidesData[0].title
//        slideDescriptionTV.text = viewModel.slidesData[0].content
//
//        val imageList = viewModel.slidesData.map { it.image }


    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        val pagesCount = pagerAdapter!!.count
        val slidesCoverageProgress = ((position + 1) * 100 )/ pagesCount
        progress.progress = slidesCoverageProgress
        toolbar.subtitle = "${position + 1} of $pagesCount"
    }

}