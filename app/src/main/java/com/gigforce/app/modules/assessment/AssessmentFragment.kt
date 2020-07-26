package com.gigforce.app.modules.assessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.ItemOffsetDecoration
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.openPopupMenu
import com.gigforce.app.utils.widgets.CustomScrollView
import kotlinx.android.synthetic.main.fragment_assessment.*
import kotlinx.android.synthetic.main.toolbar.*


/**
 * @author Rohit Sharma
 * date - 19/07/2020
 */
class AssessmentFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    AssessmentDialog.AssessmentDialogCallbacks,
    AssessmentAnswersAdapter.AssessAdapterCallbacks {
    private val viewModelAssessmentFragment by lazy {
        ViewModelProvider(this).get(ViewModelAssessmentFragment::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_assessment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTb()
        showDialog(AssessmentDialog.STATE_PASS)
        initObservers();
    }

    private fun initObservers() {
        with(viewModelAssessmentFragment) {
            observableDialogResult.observe(viewLifecycleOwner, Observer {
                val bundle = Bundle()
                bundle.putBoolean(StringConstants.ASSESSMENT_PASSED.value, it)
                navigate(R.id.assessment_result_fragment, bundle)
            })
            observableDialogInit.observe(viewLifecycleOwner, Observer {
                initialize()
            })
            observableShowHideSwipeDownIcon.observe(viewLifecycleOwner, Observer {
                iv_scroll_more_access_frag.visibility = it
            })
            observableRunSwipeDownAnim.observe(viewLifecycleOwner, Observer {
                swipeDownAnim()
            })
            observableShowHideQuestionHeader.observe(viewLifecycleOwner, Observer {
                tv_scenario_label_header_assess_frag.visibility = it
                tv_scenario_value_header_assess_frag.visibility = it
            })
        }
    }

    private fun initTb() {
        iv_options_menu_tb.visibility = View.VISIBLE
        tv_title_toolbar.text = getString(R.string.assessment)
    }

    private fun initialize() {
        initUI()
        initClicks();
        setupRecycler();
    }

    private fun setupRecycler() {
        val adapter = AssessmentAnswersAdapter()
        adapter.setCallbacks(this)
        adapter.addData(ArrayList(listOf("", "", "", "")))
        rv_options_assess_frag.adapter = adapter
        rv_options_assess_frag.layoutManager = LinearLayoutManager(activity)
        rv_options_assess_frag.addItemDecoration(ItemOffsetDecoration(context, R.dimen.size_16))

    }

    private fun initClicks() {
        iv_options_menu_tb.setOnClickListener {
            openPopupMenu(it, R.menu.menu_assessment, this, activity)
        }
    }

    private fun initUI() {
        sv_assess_frag.visibility = View.VISIBLE
        iv_scroll_more_access_frag.visibility = View.VISIBLE
        bt_next_assess_frag.visibility = View.VISIBLE
        sv_assess_frag.setScrollerListener(object : CustomScrollView.onScrollListener {
            override fun onBottomReached(reached: Boolean) {
                viewModelAssessmentFragment.bottomReached(reached)
            }

            override fun onScrollChanged() {
                viewModelAssessmentFragment.shouldQuestionHeaderBeVisible(
                    tv_scenario_label_assess_frag,
                    sv_assess_frag
                )
            }
        })
        swipeDownAnim()
    }

    private fun swipeDownAnim() {
        iv_scroll_more_access_frag.startAnimation(
            AnimationUtils.loadAnimation(
                activity,
                R.anim.swipe_down_animation
            )
        )
    }

    private fun showDialog(state: Int) {
        val dialog = AssessmentDialog.newInstance(state);
        dialog.setCallbacks(this)
        dialog.show(parentFragmentManager, AssessmentDialog::class.java.name)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        TODO("Not yet implemented")
    }

    override fun assessmentState(state: Int) {
        viewModelAssessmentFragment.switchAsPerState(state)
    }

    override fun submitAnswer() {
        sv_assess_frag.post {
            sv_assess_frag.fullScroll(View.FOCUS_DOWN)
        }
    }
}