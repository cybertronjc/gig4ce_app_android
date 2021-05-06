package com.gigforce.app.modules.explore_by_role

import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.core.datamodels.profile.Experience
import com.gigforce.common_ui.decors.ItemDecorationAddContact
import com.gigforce.common_ui.StringConstants
import kotlinx.android.synthetic.main.layout_add_experience_fragment.*

class AddExperienceFragment : BaseFragment(), AdapterAddExperience.AdapterAddEducationCallbacks {
    private var adapter: AdapterAddExperience? = null
    private lateinit var win: Window
    val addExperience: AddExperienceViewModel by activityViewModels<AddExperienceViewModel>()

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
        initObservers()
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
            navFragmentsData?.setData(
                bundleOf(
                    StringConstants.BACK_PRESSED.value to true

                )
            )
            popBackState()
        }
    }

    private fun initObservers() {
        addExperience.observableSuccess.observe(viewLifecycleOwner, Observer {
            pb_add_experience.gone()
            if (it == "true") {
                navFragmentsData?.setData(bundleOf(StringConstants.MOVE_TO_NEXT_STEP.value to true))

                popBackState()
            } else {
                showToast(it!!)
            }

        })


    }

    private fun setUpRecycler() {
        rv_add_experience.layoutManager = LinearLayoutManager(requireActivity())
        rv_add_experience.addItemDecoration(
            ItemDecorationAddContact(
                requireContext()
            )
        )
        adapter = AdapterAddExperience()
        rv_add_experience.adapter = adapter
        adapter?.addData(mutableListOf(Experience()))

        adapter?.setCallbacks(this)


    }

    override fun submitClicked(items: MutableList<Experience>) {

        var submitExperience = true
        for (i in 0 until items.size) {
            val education = items.get(i)
            items[i].validateFields = true
            if (education.title.isNullOrEmpty() || education.company.isNullOrEmpty() || education.employmentType.isNullOrEmpty() || education.location.isNullOrEmpty() || education.startDate == null || education.endDate == null) {
                submitExperience = false

            }
        }
        adapter?.notifyItemRangeChanged(0, items.size)


        if (submitExperience) {
            pb_add_experience.visible()
            addExperience.addExperience(items)
        }

    }

    override fun onBackPressed(): Boolean {
        navFragmentsData?.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true

            )
        )
        return super.onBackPressed()
    }

    override fun goBack() {
        navFragmentsData?.setData(
            bundleOf(
                StringConstants.BACK_PRESSED.value to true

            )
        )
        popBackState()
    }
}