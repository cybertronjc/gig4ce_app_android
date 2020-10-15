package com.gigforce.app.modules.explore_by_role

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.profile.models.Language
import com.gigforce.app.utils.AddLangugeRvItemDecorator
import kotlinx.android.synthetic.main.layout_fragment_add_language.*

class AddLanguageFragment : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_fragment_add_language, inflater, container)
    }

    private var adapter: AdapterAddLanguage? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycler()
    }

    private fun setUpRecycler() {
        rv_add_language.layoutManager = LinearLayoutManager(requireActivity())
        rv_add_language.addItemDecoration(AddLangugeRvItemDecorator(requireContext()))
        adapter = AdapterAddLanguage()
        rv_add_language.adapter = adapter
        adapter?.addData(mutableListOf(Language()))


    }
}