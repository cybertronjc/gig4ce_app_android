package com.gigforce.app.modules.explore_by_role

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.profile.models.Experience
import com.gigforce.app.utils.ItemDecorationAddContact
import kotlinx.android.synthetic.main.layout_add_education_fragment.*
import kotlinx.android.synthetic.main.layout_add_experience_fragment.*

class AddExperienceFragment : BaseFragment(), AdapterAddExperience.AdapterAddEducationCallbacks {
    private var adapter: AdapterAddExperience? = null
    private lateinit var win: Window

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_add_experience_fragment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpRecycler()
//        initObservers()
        initClicks()
    }


    private fun makeStatusBarTransparent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
            win.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            win.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            win.setStatusBarColor(requireActivity().getColor(R.color.white))
        }
    }


    private fun restoreStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            win = requireActivity().window
            win.clearFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
    }

    override fun onStart() {
        super.onStart()
        makeStatusBarTransparent()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        restoreStatusBar()
    }

    override fun onDestroy() {
        super.onDestroy()
        restoreStatusBar()
    }


    private fun initClicks() {
        iv_close_add_experience.setOnClickListener {
            popBackState()
        }
    }

//    private fun initObservers() {
//        addEducationViewModel.observableSuccess.observe(viewLifecycleOwner, Observer {
//            pb_add_education.gone()
//            if (it == "true") {
//                popBackState()
//            } else {
//                showToast(it!!)
//            }
//
//        })
//
//
//    }

    private fun setUpRecycler() {
        rv_add_experience.layoutManager = LinearLayoutManager(requireActivity())
        rv_add_experience.addItemDecoration(ItemDecorationAddContact(requireContext()))
        adapter = AdapterAddExperience()
        rv_add_experience.adapter = adapter
        adapter?.addData(mutableListOf(Experience()))

        adapter?.setCallbacks(this)


    }

    override fun submitClicked(items: MutableList<Experience>) {

    }
}