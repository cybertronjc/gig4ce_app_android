package com.gigforce.app.modules.assessment

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.core.gone
import com.gigforce.app.utils.*
import kotlinx.android.synthetic.main.fragment_assessment_result.*
import kotlinx.android.synthetic.main.layout_rv_question_wisr_sum_assess_result.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File
import java.util.concurrent.TimeUnit


class AssessmentResultFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    AdapterSuggestedLearning.AdapterSuggestedLearningCallbacks {
    private var adapter: RecyclerGenericAdapter<Boolean>? = null
    private val viewModelAssessmentResult by lazy {
        ViewModelProvider(this).get(ViewModelAssessmentResult::class.java)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_assessment_result, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI()
        setupRecycler()
        initObservers()
        initClicks()

    }

    private fun initObservers() {

        viewModelAssessmentResult.observableQuestionWiseSumList.observe(viewLifecycleOwner,
            Observer {

            })
        viewModelAssessmentResult.observableIsUserPassed.observe(viewLifecycleOwner, Observer {
            tv_sug_learnings_label_assess_frag.visibility = it
            rv_sug_learnings_assess_result.visibility = it
        })
        viewModelAssessmentResult.observablePermResultsGranted.observe(
            viewLifecycleOwner,
            Observer {
                initShareImage()
            })
        viewModelAssessmentResult.observablePermResultsNotGranted.observe(
            viewLifecycleOwner,
            Observer {
                checkForRequiredPermissions()
            })
        viewModelAssessmentResult.observablePermAlReadyGranted.observe(
            viewLifecycleOwner,
            Observer {
                initShareImage()
            })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModelAssessmentResult.onActivityResultCalled(requestCode, resultCode, data)
    }

    private fun setupRecycler() {
        adapter = RecyclerGenericAdapter<Boolean>(context,
            PFRecyclerViewAdapter.OnViewHolderClick<Boolean> { view, position, item -> },
            RecyclerGenericAdapter.ItemInterface { obj, viewHolder, position ->

                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.text = "" + (position + 1)
                viewHolder.itemView.fl_rv_question_wise_sum_assess_result.setSolidColor(if (obj) "#ffd9e6" else "#888888")

                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.setTextColor(
                    if (obj) activity?.getColor(R.color.darkish_pink_100)!! else activity?.getColor(
                        R.color.black_85
                    )!!
                )
                viewHolder.itemView.tv_q_no_rv_ques_sum_assess_result.isSelected = true

            })

        adapter?.setLayout(R.layout.layout_rv_question_wisr_sum_assess_result)
        rv_question_wise_sum_assess_frag.setHasFixedSize(true)
        rv_question_wise_sum_assess_frag.layoutManager = GridLayoutManager(activity, 5)
        rv_question_wise_sum_assess_frag.addItemDecoration(
            ItemDecor(
                resources.getDimensionPixelSize(
                    R.dimen.size_10
                ),
                5
            )
        )
        rv_question_wise_sum_assess_frag.adapter = adapter
        adapter?.addAll(arguments?.getBooleanArray(StringConstants.ANSWERS_ARR.value)?.toList())

        rv_sug_learnings_assess_result.gone()
        rv_sug_learnings_assess_result.adapter = AdapterSuggestedLearning().setCallbacks(this)
        rv_sug_learnings_assess_result.layoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rv_sug_learnings_assess_result.addItemDecoration(
            HorizontaltemDecoration(
                activity,
                R.dimen.size_16
            )
        )


    }

    private fun initClicks() {
        iv_options_menu_tb.setOnClickListener {
            openPopupMenu(it, R.menu.menu_assessment_result, this, activity)
        }
        iv_back.setOnClickListener {
            popTillSecondLastFragment()
        }
    }

    override fun onBackPressed(): Boolean {
        popTillSecondLastFragment()
        return false
    }

    private fun popTillSecondLastFragment() {
        val index = parentFragmentManager.backStackEntryCount - 2
        val backEntry = parentFragmentManager.getBackStackEntryAt(index);
        val tag = backEntry.name;
        val fragmentManager: FragmentManager? = parentFragmentManager
        fragmentManager?.executePendingTransactions()
        fragmentManager?.popBackStack(tag, FragmentManager.POP_BACK_STACK_INCLUSIVE)


    }

    private fun initUI() {
        tv_title_toolbar.text = getString(R.string.assessment)
        iv_options_menu_tb.visibility = View.VISIBLE
        var correctAns = 0
        arguments?.getBooleanArray(StringConstants.ANSWERS_ARR.value)?.forEach { item ->
            run {
                if (item) correctAns++
            }
        }
        var percent = String.format(
            "%.1f",
            (((correctAns / arguments?.getBooleanArray(StringConstants.ANSWERS_ARR.value)?.size?.toFloat()!!) * 100))
        ) + " %"
        tv_score_assess_result.text =
            Html.fromHtml("You have scored <b>$percent</b> in your assessment")
        tv_new_cert_asses_frag.text =
            Html.fromHtml("<u>New certificate has been added to profile .</u>")
        viewModelAssessmentResult.checkIfUserPassed(arguments?.getBoolean(StringConstants.ASSESSMENT_PASSED.value))
        iv_options_menu_tb.visibility =
            if (arguments?.getBoolean(
                    StringConstants.ASSESSMENT_PASSED.value,
                    false
                )!!
            ) View.VISIBLE else View.GONE

        val timeTaken = arguments?.getInt(StringConstants.TIME_TAKEN.value)?.toLong()!!
        tv_time_taken_value_assess_frag.text = String.format(
            " %02d hrs %02d min %02d sec", TimeUnit.MILLISECONDS.toHours(timeTaken),
            TimeUnit.MILLISECONDS.toMinutes(timeTaken) % TimeUnit.HOURS.toMinutes(1),
            TimeUnit.MILLISECONDS.toSeconds(timeTaken) % TimeUnit.MINUTES.toSeconds(1)
        );
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                viewModelAssessmentResult.checkForPermissionsAndInitSharing(
                    checkForRequiredPermissions()
                ); return true
            }
        }
        return false
    }

    private fun checkForRequiredPermissions(): Boolean {
        return PermissionUtils.checkForPermissionFragment(
            this,
            PermissionUtils.reqCodePerm,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    }

    fun initShareImage() {
        storeImage(
            getScreenShot(cl_sv_nested_assess_result),
            StringConstants.CERTIFICATE_SSC.value,
            context?.filesDir?.absolutePath!!

        )
        shareFile(
            File(context?.filesDir?.absolutePath + "/" + StringConstants.CERTIFICATE_SSC.value),
            requireContext(),
            "image/*"
        )
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        viewModelAssessmentResult.checkIfPermGranted(requestCode, grantResults)
    }


    override fun onClickSuggestedLearnings() {
        navigate(R.id.mainLearningFragment)
    }


}