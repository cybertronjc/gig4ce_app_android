package com.gigforce.app.modules.earn

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.GigHistoryItemDecorator
import com.gigforce.app.utils.ItemOffsetDecoration
import kotlinx.android.synthetic.main.fragment_gig_history.*


/**
 * A simple [Fragment] subclass.
 * Use the [GigHistoryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GigHistoryFragment : BaseFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflateView(R.layout.fragment_gig_history, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv_gigs_gig_history.adapter = AdapterGigHistory()
        rv_gigs_gig_history.layoutManager = LinearLayoutManager(activity)
        rv_gigs_gig_history.addItemDecoration(
            GigHistoryItemDecorator(
                requireContext().resources.getDimensionPixelOffset(
                    R.dimen.size_16
                )
            )
        )
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            GigHistoryFragment()
    }

}