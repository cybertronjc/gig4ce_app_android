package com.gigforce.learning.learning.slides

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.viewpager.widget.ViewPager
import com.example.learning.R
import com.gigforce.learning.learning.slides.types.VideoFragmentOrientationListener
import com.gigforce.common_ui.core.IOnBackPressedOverride
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.core.utils.Lce
import kotlinx.android.synthetic.main.fragment_slides.*
import kotlinx.android.synthetic.main.fragment_slides_main.*

class SlidesFragment : Fragment(),
    ViewPager.OnPageChangeListener,
    IOnBackPressedOverride,
    VideoFragmentOrientationListener {

    private lateinit var mLessonId: String
    private lateinit var mModuleId: String
    private lateinit var toolbarTitle: String

    private val viewModel: SlideViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_slides, container)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        savedInstanceState?.let {

            toolbarTitle = it.getString(INTENT_EXTRA_SLIDE_TITLE) ?: ""
            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let

            toolbar.title = toolbarTitle
        }

        arguments?.let {

            toolbarTitle = it.getString(INTENT_EXTRA_SLIDE_TITLE) ?: ""
            mLessonId = it.getString(INTENT_EXTRA_LESSON_ID) ?: return@let
            mModuleId = it.getString(INTENT_EXTRA_MODULE_ID) ?: return@let

            toolbar.title = toolbarTitle
        }

        initView()
        initViewModel()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_LESSON_ID, mLessonId)
        outState.putString(INTENT_EXTRA_MODULE_ID, mModuleId)
        outState.putString(INTENT_EXTRA_SLIDE_TITLE, toolbarTitle)
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
            moduleId = mModuleId,
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

    private fun showSlides(content: SlideInfoAndDirection) {

        fragment_slides_progress_bar.gone()
        fragment_slides_error.gone()
        fragment_slides_main_layout.visible()

        pagerAdapter = SlidesPagerAdapter(childFragmentManager,mModuleId,mLessonId, content.slideContent, this)
        slideViewPager.adapter = pagerAdapter
        slideViewPager.addOnPageChangeListener(this)

        slideViewPager.setCurrentItem(content.activeSlideIndex, true)

        showDots(content.slideContent.size)
    }

    private fun showDots(dotsCount: Int) {
        sliderDotsContainer.removeAllViews()

        for (i in 0..dotsCount) {

            val dotIV = ImageView(requireContext())

            if (i == 0) {
                dotIV.setImageResource(R.drawable.ic_dot_active)
            } else {
                dotIV.setImageResource(R.drawable.ic_dot_inactive)
            }

            val layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            layoutParams.leftMargin = 8
            layoutParams.rightMargin = 8
            sliderDotsContainer.addView(dotIV, layoutParams)
        }
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
        setDotAsSelected(position)
    }

    private fun setDotAsSelected(position: Int) {
        for (i in 0 until sliderDotsContainer.childCount) {

            val dotIV = sliderDotsContainer.getChildAt(i) as ImageView
            if (i == position)
                dotIV.setImageResource(R.drawable.ic_dot_active)
            else
                dotIV.setImageResource(R.drawable.ic_dot_inactive)

        }
    }

    override fun onBackPressed(): Boolean {
        return pagerAdapter?.dispatchOnBackPressedIfCurrentFragmentIsVideoFragment(slideViewPager.currentItem)
            ?: false
    }

    companion object {
        const val INTENT_EXTRA_SLIDE_TITLE = "slide_title"
        const val INTENT_EXTRA_LESSON_ID = "lesson_id"
        const val INTENT_EXTRA_MODULE_ID = "module_id"
    }
}