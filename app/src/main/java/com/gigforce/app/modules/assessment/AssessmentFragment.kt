package com.gigforce.app.modules.assessment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.openPopupMenu
import kotlinx.android.synthetic.main.toolbar.*


/**
 * @author Rohit Sharma
 * date - 19/07/2020
 */
class AssessmentFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assessment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        initClicks();
        showDialog(AssessmentDialog.STATE_INIT)
    }

    private fun initClicks() {
        iv_options_menu_tb.setOnClickListener {
            openPopupMenu(it,R.menu.menu_assessment,this,activity)
        }

    }

    private fun initUI() {
        tv_title_toolbar.text = getString(R.string.assessment)
        iv_options_menu_tb.visibility = View.VISIBLE

    }

    private fun showDialog(state: Int) {
        when (state) {
            AssessmentDialog.STATE_INIT -> AssessmentDialog.newInstance()
                .show(parentFragmentManager, AssessmentDialog::class.java.name)
        }

    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        TODO("Not yet implemented")
    }


}