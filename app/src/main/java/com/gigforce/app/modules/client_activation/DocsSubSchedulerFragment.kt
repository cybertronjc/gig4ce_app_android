package com.gigforce.app.modules.client_activation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DocsSubSchedulerFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DocsSubSchedulerFragment : BaseFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflateView(R.layout.fragment_docs_sub_scheduler, inflater, container)
    }

    companion object {
        fun newInstance() = DocsSubSchedulerFragment()
    }
}