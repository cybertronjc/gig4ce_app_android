package com.gigforce.app.modules.assessment

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.core.genericadapter.PFRecyclerViewAdapter
import com.gigforce.app.core.genericadapter.RecyclerGenericAdapter
import com.gigforce.app.utils.HorizontaltemDecoration
import com.gigforce.app.utils.ItemDecor
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.openPopupMenu
import kotlinx.android.synthetic.main.fragment_assessment_result.*
import kotlinx.android.synthetic.main.layout_rv_question_wisr_sum_assess_result.view.*
import kotlinx.android.synthetic.main.toolbar.*
import java.io.File


class AssessmentResultFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener {
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
                adapter?.addAll(it)
            })
        viewModelAssessmentResult.observableIsUserPassed.observe(viewLifecycleOwner, Observer {
            tv_sug_learnings_label_assess_frag.visibility = it
            rv_sug_learnings_assess_result.visibility = it
        })
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
        viewModelAssessmentResult.getQuestionWiseSumData()
        rv_sug_learnings_assess_result.adapter = AdapterSuggestedLearning()
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
    }

    private fun initUI() {
        tv_title_toolbar.text = getString(R.string.assessment)
        iv_options_menu_tb.visibility = View.VISIBLE
        tv_score_assess_result.text = Html.fromHtml("You have scored <b>70%</b> in your assessment")
        tv_new_cert_asses_frag.text =
            Html.fromHtml("<u>New certificate has been added to profile .</u>")
        viewModelAssessmentResult.checkIfUserPassed(arguments?.getBoolean(StringConstants.ASSESSMENT_PASSED.value))
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        when (item?.itemId) {
            R.id.action_share -> {
                viewModelAssessmentResult.store(
                    getScreenShot(cl_sv_nested_assess_result),
                    StringConstants.CERTIFICATE_SSC.value, context?.filesDir?.absolutePath!!
                )
                shareImage(File(context?.filesDir?.absolutePath + "/" + StringConstants.CERTIFICATE_SSC.value))
                return true
            }
        }
        return false
    }

    private fun shareImage(file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            requireContext(),
            requireContext().packageName + ".provider",
            file
        )
        val intent = Intent()
        intent.action = Intent.ACTION_SEND
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_SUBJECT, "")
        intent.putExtra(Intent.EXTRA_TEXT, "")
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        try {
            startActivity(Intent.createChooser(intent, "Share Certificate"))
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, "No App Available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getScreenShot(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas)
        else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }

}