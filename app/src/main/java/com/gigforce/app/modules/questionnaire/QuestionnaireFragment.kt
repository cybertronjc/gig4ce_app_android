package com.gigforce.app.modules.questionnaire

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.RVPagerSnapFancyDecorator
import com.gigforce.app.utils.RatioLayoutManager
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_questionnaire_fragment.*


class QuestionnaireFragment : BaseFragment() {
    private lateinit var viewModel: ViewModelQuestionnaire
    private val adapter: AdapterQuestionnaire by lazy {
        AdapterQuestionnaire()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.layout_questionnaire_fragment, inflater, container)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel =
                ViewModelProvider(
                        this,
                        SavedStateViewModelFactory(requireActivity().application, this)
                ).get(ViewModelQuestionnaire::class.java)
        setupRecycler()
        initObservers()
    }

    private fun initObservers() {
        viewModel.observableQuestionnaireResponse.observe(viewLifecycleOwner, Observer {
            setupTabs(it.questions?.size ?: 0)
            adapter.addData(it.questions!!)
        })
        if (!viewModel.initialized) {
            viewModel.getQuestionnaire()
        }


    }

    private fun setupRecycler() {
        rv_questionnaire.adapter = adapter
        val ratioToCover = 0.85f
        val ratioLayoutManager = RatioLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false, ratioToCover)
        rv_questionnaire.layoutManager = ratioLayoutManager
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rv_questionnaire)
        rv_questionnaire.addItemDecoration(RVPagerSnapFancyDecorator(requireContext(), (getScreenWidth(requireActivity()).width * ratioToCover).toInt(), 0.015f))
        rv_questionnaire.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                tb_layout_questionnaire.getTabAt(ratioLayoutManager.findFirstCompletelyVisibleItemPosition())?.select();
            }
        })

    }

    private fun setupTabs(size: Int) {
        tb_layout_questionnaire.removeAllTabs()
        for (i in 0 until size)
            tb_layout_questionnaire.addTab(tb_layout_questionnaire.newTab())

    }

}