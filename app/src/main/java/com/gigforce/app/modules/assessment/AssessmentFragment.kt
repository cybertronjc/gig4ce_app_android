package com.gigforce.app.modules.assessment

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.ScrollView
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.assessment.models.AssementQuestionsReponse
import com.gigforce.app.utils.ItemOffsetDecoration
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.ViewModelProviderFactory
import com.gigforce.app.utils.openPopupMenu
import com.gigforce.app.utils.widgets.CustomScrollView
import kotlinx.android.synthetic.main.fragment_assessment.*
import kotlinx.android.synthetic.main.toolbar.*


/**
 * @author Rohit Sharma
 * date - 19/07/2020
 */
class AssessmentFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    AssessmentDialog.AssessmentDialogCallbacks,
    AssessmentAnswersAdapter.AssessAdapterCallbacks {

    private var adapter: AssessmentAnswersAdapter? = null
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(ViewModelAssessmentFragment(ModelAssessmentFragment()))
    }
    private val viewModelAssessmentFragment: ViewModelAssessmentFragment by lazy {
        ViewModelProvider(this, viewModelFactory).get(ViewModelAssessmentFragment::class.java)
    }
    private var selectedPosition: Int = 0


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_assessment, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initTb()
        initObservers();
        setupRecycler();
        viewModelAssessmentFragment.getQuestionaire()
    }

    private fun initObservers() {
        with(viewModelAssessmentFragment) {
            observableDialogResult.observe(viewLifecycleOwner, Observer {
                val bundle = Bundle()
                bundle.putBoolean(StringConstants.ASSESSMENT_PASSED.value, it)
                val arr =
                    BooleanArray(viewModelAssessmentFragment.observableAssessmentData.value?.assessment!!.size) { false }
                viewModelAssessmentFragment.observableAssessmentData.value!!.assessment!!.forEachIndexed() { index, elem ->
                    run {
                        elem.options?.forEach { elem ->
                            run {
                                if (elem.selectedAnswer == true && elem.is_answer == true) {
                                    arr[index] = true
                                }
                            }
                        }
                    }
                }

                bundle.putBooleanArray(StringConstants.ANSWERS_ARR.value, arr)
                navigate(R.id.assessment_result_fragment, bundle)
            })
            observableDialogInit.observe(viewLifecycleOwner, Observer {
                initialize()


            })
            observableShowHideSwipeDownIcon.observe(viewLifecycleOwner, Observer {
                iv_scroll_more_access_frag.visibility = it
            })
            observableRunSwipeDownAnim.observe(viewLifecycleOwner, Observer {
                swipeDownAnim()
            })
            observableShowHideQuestionHeader.observe(viewLifecycleOwner, Observer {
//                tv_scenario_label_header_assess_frag.visibility = it
//                tv_scenario_value_header_assess_frag.visibility = it
            })
            observableAssessmentData.observe(viewLifecycleOwner, Observer {
                showDialog(
                    AssessmentDialog.STATE_INIT, bundleOf(
                        StringConstants.DURATION.value to it.duration,
                        StringConstants.ASSESSMENT_NAME.value to it.assessment_name,
                        StringConstants.QUESTIONS_COUNT.value to (it.assessment?.size ?: 0),
                        StringConstants.LEVEL.value to it.level,
                        StringConstants.ASSESSMENT_DIALOG_STATE.value to AssessmentDialog.STATE_INIT

                    )
                )
                tv_scenario_value_assess_frag.text = it.scenario
                tv_scenario_value_header_assess_frag.text = it.scenario
                tv_level_assess_frag.text = "${getString(R.string.level)} ${it.level}"
                tv_designation_assess_frag.text = it.assessment_name
                h_pb_assess_frag.max = it.assessment?.size!!
                h_pb_assess_frag.progress = 0
                tv_percent_assess_frag.text = "0 %"
                setDataAsPerPosition(it)


            })

        }
    }

    fun setDataAsPerPosition(it: AssementQuestionsReponse) {
        tv_ques_no_assess_frag.text =
            "${getString(R.string.ques)} ${selectedPosition + 1}/${it.assessment?.size} "
        adapter?.addData(it.assessment!![selectedPosition].options!!)


    }

    private fun initTb() {
        iv_options_menu_tb.visibility = View.VISIBLE
        tv_title_toolbar.text = getString(R.string.assessment)
    }

    private fun initialize() {
        initUI()
        initClicks();


    }

    private fun setupRecycler() {
        adapter = AssessmentAnswersAdapter()
        adapter?.setCallbacks(this)

        rv_options_assess_frag.adapter = adapter
        rv_options_assess_frag.setHasFixedSize(true)
        rv_options_assess_frag.layoutManager = LinearLayoutManager(activity)
        rv_options_assess_frag.addItemDecoration(ItemOffsetDecoration(context, R.dimen.size_16))

    }

    private fun initClicks() {
        iv_options_menu_tb.setOnClickListener {
            openPopupMenu(it, R.menu.menu_assessment, this, activity)
        }
        iv_back.setOnClickListener {
            popBackState()
        }
        bt_next_assess_frag.setOnClickListener {
            if (selectedPosition == viewModelAssessmentFragment.observableAssessmentData.value?.assessment!!.size - 1) {
                if (viewModelAssessmentFragment.observableAssessmentData.value?.assessment!![selectedPosition].answered) {
                    h_pb_assess_frag.progress = h_pb_assess_frag.max
                    tv_percent_assess_frag.text = "100%"
                    var answers = 0;
                    viewModelAssessmentFragment.observableAssessmentData.value!!.assessment!!.forEach { elem ->
                        run {
                            elem.options?.forEach { elem ->
                                run {
                                    if (elem.selectedAnswer == true && elem.is_answer == true) {
                                        answers++;
                                    }
                                }
                            }
                        }
                    }
                    val questions =
                        viewModelAssessmentFragment.observableAssessmentData.value!!.assessment!!.size

                    showDialog(
                        AssessmentDialog.STATE_PASS, bundleOf(

                            StringConstants.RIGHT_ANSWERS.value to answers,
                            StringConstants.ASSESSMENT_DIALOG_STATE.value to if (answers >= viewModelAssessmentFragment.observableAssessmentData.value!!.assessment!!.size / 2) AssessmentDialog.STATE_PASS else AssessmentDialog.STATE_REAPPEAR,
                            StringConstants.QUESTIONS_COUNT.value to questions

                        )
                    )
                } else {
                    showToast(getString(R.string.answer_the_ques))

                }

            } else {
                if (viewModelAssessmentFragment.observableAssessmentData.value?.assessment!![selectedPosition].answered) {
                    ++selectedPosition
                    setDataAsPerPosition(viewModelAssessmentFragment.observableAssessmentData.value!!)
                    h_pb_assess_frag.progress = selectedPosition
                    tv_percent_assess_frag.text =
                        String.format(
                            "%.1f",
                            (((selectedPosition.toFloat() / viewModelAssessmentFragment.observableAssessmentData.value?.assessment!!.size.toFloat()).toFloat() * 100))
                        ) + " %"

                    sv_assess_frag.postDelayed(
                        Runnable { sv_assess_frag.fullScroll(ScrollView.FOCUS_UP) },
                        600
                    )
                } else {
                    showToast(getString(R.string.answer_the_ques))
                }
            }

        }
    }

    private fun initUI() {
        sv_assess_frag.visibility = View.VISIBLE
        iv_scroll_more_access_frag.visibility = View.VISIBLE
        bt_next_assess_frag.visibility = View.VISIBLE
        sv_assess_frag.setScrollerListener(object : CustomScrollView.onScrollListener {
            override fun onBottomReached(reached: Boolean) {
                viewModelAssessmentFragment.bottomReached(reached)
            }

            override fun onScrollChanged() {
                val scrollBounds = Rect()
                sv_assess_frag.getDrawingRect(scrollBounds)
                val top = tv_scenario_label_assess_frag?.y
                val bottom = top?.plus(tv_scenario_label_assess_frag?.height!!)
                viewModelAssessmentFragment.shouldQuestionHeaderBeVisible(top, bottom, scrollBounds)
            }
        })
        swipeDownAnim()
    }

    private fun swipeDownAnim() {
        iv_scroll_more_access_frag.startAnimation(
            AnimationUtils.loadAnimation(
                activity,
                R.anim.swipe_down_animation
            )
        )
    }

    private fun showDialog(
        state: Int,
        bundle: Bundle?
    ) {
        val dialog = AssessmentDialog.newInstance(state);
        dialog.setCallbacks(this)
        dialog.arguments = bundle
        dialog.show(parentFragmentManager, AssessmentDialog::class.java.name)
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        TODO("Not yet implemented")
    }

    override fun assessmentState(state: Int) {
        viewModelAssessmentFragment.switchAsPerState(state)
    }

    override fun submitAnswer() {
        sv_assess_frag.post {
            sv_assess_frag.fullScroll(View.FOCUS_DOWN)
        }
    }

    override fun setAnswered(boolean: Boolean, position: Int) {
        viewModelAssessmentFragment.observableAssessmentData.value?.assessment!![selectedPosition].answered =
            true
        val optionsArr =
            viewModelAssessmentFragment.observableAssessmentData.value?.assessment!![selectedPosition].options

        val iterate = optionsArr?.listIterator()
        while (iterate?.hasNext() == true) {
            val obj = iterate.next()
//            if (obj.selectedAnswer != true && obj.is_answer != true) {
//                iterate.remove()
//            } else {
            obj.clickStatus = false
//            }

        }
        adapter?.addData(optionsArr ?: arrayListOf())
        sv_assess_frag.postDelayed(Runnable {
            val y: Float =
                rv_options_assess_frag.y + rv_options_assess_frag.getChildAt(position).y
            val v = rv_options_assess_frag.findViewHolderForAdapterPosition(position)
            sv_assess_frag.post {
                sv_assess_frag.smoothScrollTo(0, y.toInt())

            }
        },600)


    }

    interface AssessFragmentCallbacks {
        fun getAnsweredStatus(): Boolean
    }


}