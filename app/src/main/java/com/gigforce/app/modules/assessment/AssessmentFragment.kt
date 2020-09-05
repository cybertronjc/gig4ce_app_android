package com.gigforce.app.modules.assessment

import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.PopupMenu
import android.widget.ScrollView
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.request.target.CustomTarget
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.modules.assessment.models.AssementQuestionsReponse
import com.gigforce.app.modules.profile.ProfileViewModel
import com.gigforce.app.utils.*
import com.gigforce.app.utils.widgets.CustomScrollView
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.fragment_assessment.*
import kotlinx.android.synthetic.main.toolbar.*
import java.text.SimpleDateFormat


/**
 * @author Rohit Sharma
 * date - 19/07/2020
 */
class AssessmentFragment : BaseFragment(), PopupMenu.OnMenuItemClickListener,
    AssessmentDialog.AssessmentDialogCallbacks,
    AssessmentAnswersAdapter.AssessAdapterCallbacks {

    private var pushfinalEvent: Boolean = false
    private var countDownTimer: CountDownTimer? = null
    private var adapter: AssessmentAnswersAdapter? = null
    private val viewModelFactory by lazy {
        ViewModelProviderFactory(ViewModelAssessmentFragment(ModelAssessmentFragment()))
    }
    private val viewModelAssessmentFragment: ViewModelAssessmentFragment by lazy {
        ViewModelProvider(this, viewModelFactory).get(ViewModelAssessmentFragment::class.java)
    }
    private val viewModelProfile: ProfileViewModel by lazy {
        ViewModelProvider(this).get(ProfileViewModel::class.java)
    }
    private var selectedPosition: Int = 0
    private var timeTaken = 0;


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
                bundle.putInt(StringConstants.TIME_TAKEN.value, timeTaken)
                countDownTimer?.cancel();
                navigate(R.id.assessment_result_fragment, bundle)
            })
            observableDialogInit.observe(viewLifecycleOwner, Observer {
                viewModelProfile.getProfileData().observe(viewLifecycleOwner, Observer {
                    initialize()
                })


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
                loadImage(it.assessment_image)


            })

            observableError.observe(viewLifecycleOwner, Observer {
                showToast(it)
            })
            observableQuizSubmit.observe(viewLifecycleOwner, Observer {
                finalResult()
            })

        }
    }

    fun setDataAsPerPosition(it: AssementQuestionsReponse) {
        tv_ques_no_assess_frag.text =
            "${getString(R.string.ques)} ${selectedPosition + 1}/${it.assessment?.size} "
        adapter?.addData(it.assessment!![selectedPosition].options!!, false, "")
        val sdf = SimpleDateFormat("hh:mm:ss")
        val date = sdf.parse(it.duration)

        initCountDownTimer(miliseconds(date.hours, date.minutes, date.seconds))


    }

    fun miliseconds(hrs: Int, min: Int, sec: Int): Long {
        return (((hrs * 60 * 60 + min * 60 + sec) * 1000).toLong());
    }

    private fun initTb() {
        iv_options_menu_tb.visibility = View.VISIBLE
        tv_title_toolbar.text = getString(R.string.assessment)
    }

    private fun initialize() {
        initUI()
        initClicks();
        countDownTimer?.start();

    }

    private fun initCountDownTimer(millis: Long) {
        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeTaken += 1000
            }

            override fun onFinish() {

                if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
                    pushfinalEvent = true
                    return
                }
                showToast(getString(R.string.time_is_up))
                timeTaken= millis.toInt()
                viewModelAssessmentFragment.observableAssessmentData.value?.timeTakenInMillis =
                    timeTaken.toLong();
                viewModelAssessmentFragment.submitAnswers(viewModelProfile.getProfileData().value?.id)
            }
        }

    }

    override fun onPause() {
        super.onPause()

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
                    tv_percent_assess_frag.text = getString(R.string.hundred_percent)
                    viewModelAssessmentFragment.observableAssessmentData.value?.timeTakenInMillis =
                        timeTaken.toLong();
                    viewModelAssessmentFragment.submitAnswers(viewModelProfile.getProfileData().value?.id)

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

    override fun onResume() {
        super.onResume()
        if (pushfinalEvent) {
            pushfinalEvent = false
            Handler().postDelayed({
                showToast(getString(R.string.time_is_up))
                viewModelAssessmentFragment.observableAssessmentData.value?.timeTakenInMillis =
                    timeTaken.toLong();
                viewModelAssessmentFragment.submitAnswers(viewModelProfile.getProfileData().value?.id)
            }, 500)

        }
    }

    private fun finalResult() {

        if (!lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) {
            pushfinalEvent = true
            return
        }
        countDownTimer?.cancel()
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
        var isPassed =
            (answers / questions.toFloat()) * 100 >= viewModelAssessmentFragment.observableAssessmentData.value?.passing_percentage!!

        showDialog(
            AssessmentDialog.STATE_PASS, bundleOf(
                StringConstants.RIGHT_ANSWERS.value to answers,
                StringConstants.ASSESSMENT_DIALOG_STATE.value to if (isPassed) AssessmentDialog.STATE_PASS else AssessmentDialog.STATE_REAPPEAR,
                StringConstants.QUESTIONS_COUNT.value to questions

            )
        )

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

    override fun setAnswered(isCorrect: Boolean, position: Int) {
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
        adapter?.addData(
            optionsArr ?: arrayListOf(),
            true,
            if (isCorrect) getString(R.string.woe_you_are_correct) else getString(
                R.string.you_are_incorrect
            )
        )

        val y: Float =
            rv_options_assess_frag.y + rv_options_assess_frag.getChildAt(position).y
        sv_assess_frag.post {
            sv_assess_frag.fullScroll(View.FOCUS_DOWN)
        }


    }

    interface AssessFragmentCallbacks {
        fun getAnsweredStatus(): Boolean
    }

    private fun loadImage(Path: String) {

        val reference: StorageReference =
            FirebaseStorage.getInstance().reference.child("assessment_images").child(Path)

        GlideApp.with(this.requireContext())
            .asBitmap()
            .load(reference)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    iv_scenario_value_assess_frag.setImageBitmap(resource);
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                }

            })

    }

    override fun onBackPressed(): Boolean {

        countDownTimer?.cancel()

        return super.onBackPressed()
    }


}