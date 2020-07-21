package com.gigforce.app.modules.assessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.ItemOffsetDecoration
import com.gigforce.app.utils.openPopupMenu
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

        rv_options_assess_frag.adapter = AssessmentAnswersAdapter()
        rv_options_assess_frag.layoutManager = LinearLayoutManager(activity)
        rv_options_assess_frag.addItemDecoration(
            ItemOffsetDecoration(
                context, R.dimen.size_16
            )
        )
    }

    private fun initClicks() {
        iv_options_menu_tb.setOnClickListener {
            openPopupMenu(it, R.menu.menu_assessment, this, activity)
        }

    }

    private fun initUI() {
        sv_assess_frag.visibility = View.VISIBLE
        bt_next_assess_frag.visibility = View.VISIBLE

    }

    private fun showDialog(state: Int) {
        when (state) {
            AssessmentDialog.STATE_INIT -> {

                val dialog: AssessmentDialog = AssessmentDialog.newInstance(AssessmentDialog.STATE_PASS);
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


}