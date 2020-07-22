package com.gigforce.app.modules.assessment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.auth.AuthUI.getApplicationContext
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.ItemOffsetDecoration
import com.gigforce.app.utils.openPopupMenu
import com.gigforce.app.utils.widgets.CustomScrollView
import kotlinx.android.synthetic.main.fragment_assessment.*
import kotlinx.android.synthetic.main.toolbar.*


/**
 * @author Rohit Sharma
 * date - 19/07/2020
 */
class AssessmentFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    AssessmentDialog.AssessmentDialogCallbacks {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assessment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTb()
        showDialog(AssessmentDialog.STATE_INIT)

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
        var adapter = AssessmentAnswersAdapter()
        rv_options_assess_frag.adapter = adapter
        rv_options_assess_frag.layoutManager = LinearLayoutManager(activity)
        rv_options_assess_frag.addItemDecoration(
            ItemOffsetDecoration(
                context, R.dimen.size_16
            )
        )
        adapter.addData(ArrayList(listOf("", "", "", "")))

    }

    private fun initClicks() {
        iv_options_menu_tb.setOnClickListener {
            openPopupMenu(it, R.menu.menu_assessment, this, activity)
        }

    }

    private fun initUI() {
        sv_assess_frag.visibility = View.VISIBLE
        bt_next_assess_frag.visibility = View.VISIBLE
        sv_assess_frag.setScrollerListener(object : CustomScrollView.onScrollListener {
            override fun onBottomReached(isBottom: Boolean) {
                iv_scroll_more_access_frag.visibility = if (isBottom) View.GONE else View.VISIBLE
                if (!isBottom) {
                    iv_scroll_more_access_frag.startAnimation(
                        AnimationUtils.loadAnimation(
                            activity,
                            R.anim.swipe_down_animation
                        )
                    )
                }
            }

            override fun onScrollChanged() {
                var isVisible = isQuestionVisible(tv_scenario_label_assess_frag)
                tv_scenario_label_header_assess_frag.visibility =
                    if (isVisible) View.GONE else View.VISIBLE
                tv_scenario_value_header_assess_frag.visibility =
                    if (isVisible) View.GONE else View.VISIBLE
            }

        })
        iv_scroll_more_access_frag.startAnimation(
            AnimationUtils.loadAnimation(
                activity,
                R.anim.swipe_down_animation
            )
        )


    }

    private fun showDialog(state: Int) {
        when (state) {
            AssessmentDialog.STATE_INIT -> {
                val dialog: AssessmentDialog =
                    AssessmentDialog.newInstance(AssessmentDialog.STATE_INIT);
                dialog.setCallbacks(this)
                dialog.show(parentFragmentManager, AssessmentDialog::class.java.name)
            }
        }

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        TODO("Not yet implemented")
    }

    override fun initAssessment() {
        initialize()

    }

    private fun isQuestionVisible(view: View): Boolean {
        val scrollBounds = Rect()
        sv_assess_frag.getDrawingRect(scrollBounds)
        val top = view.y
        val bottom = top + view.height
        return scrollBounds.top < top && scrollBounds.bottom > bottom
    }


}