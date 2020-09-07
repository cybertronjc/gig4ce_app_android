package com.gigforce.app.modules.learning.slides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.modules.learning.models.SlideContent
import com.gigforce.app.modules.learning.slides.types.VideoFragmentOrientationListener
import com.gigforce.app.utils.Lce
import kotlinx.android.synthetic.main.fragment_slides.*
import kotlinx.android.synthetic.main.fragment_slides_main.*


class SlidesFragment : BaseFragment(), ViewPager.OnPageChangeListener,
    VideoFragmentOrientationListener {

    private lateinit var mLessonId: String

    private val viewModel: SlideViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflateView(R.layout.fragment_slides, inflater, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {

            val slideTitle = it.getString(INTENT_EXTRA_SLIDE_TITLE)
            toolbar.title = slideTitle

            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID)?: return@let
        }

        arguments?.let {

            val slideTitle = it.getString(INTENT_EXTRA_SLIDE_TITLE)
            toolbar.title = slideTitle

            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID)?: return@let
        }

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
            lessonId = mLessonId
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

        pagerAdapter = SlidesPagerAdapter(childFragmentManager, content, this)
        slideViewPager.adapter = pagerAdapter
        slideViewPager.addOnPageChangeListener(this)

        val slidesCoverageProgress = 100 / content.size
        progress.progress = slidesCoverageProgress
    }

    private fun showSlidesLoadingLayout() {

        fragment_slides_main_layout.gone()
        fragment_slides_error.gone()
        fragment_slides_progress_bar.visible()
    }

    private fun initView() {

        toolbar.setNavigationOnClickListener {
            activity?.onBackPressed()
        }


//        slideCounterTV.text = "Slide 1 of ${viewModel.slidesData.size}"
//        slideTitleTV.text = viewModel.slidesData[0].title
//        slideDescriptionTV.text = viewModel.slidesData[0].content
//
//        val imageList = viewModel.slidesData.map { it.image }


    }

    override fun onOrientationChange(landscape: Boolean) {
        appBar.isVisible = !landscape
    }

    override fun onPageScrollStateChanged(state: Int) {}
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

    override fun onPageSelected(position: Int) {
        val pagesCount = pagerAdapter!!.count
        val slidesCoverageProgress = ((position + 1) * 100) / pagesCount
        progress.progress = slidesCoverageProgress
    }

    override fun onBackPressed(): Boolean {
        return pagerAdapter?.dispatchOnBackPressedIfCurrentFragmentIsVideoFragment(slideViewPager.currentItem)
            ?: false
    }

    companion object {
        const val INTENT_EXTRA_SLIDE_TITLE = "slide_title"
        const val INTENT_EXTRA_LESSON_ID = "lesson_id"
    }
}