package com.gigforce.app.modules.questionnaire

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.*
import androidx.recyclerview.widget.RecyclerView.SmoothScroller
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.core.extensions.gone
import com.gigforce.core.extensions.visible
import com.gigforce.client_activation.client_activation.DrivingCertSuccessDialog
import com.gigforce.client_activation.client_activation.RejectionDialog
import com.gigforce.client_activation.client_activation.models.Cities
import com.gigforce.client_activation.client_activation.models.States
//import com.gigforce.landing_screen.landingscreen.models.Dependency
import com.gigforce.app.modules.questionnaire.models.Questions
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.decors.RVPagerSnapFancyDecorator
import com.gigforce.common_ui.ext.showToast
import com.gigforce.common_ui.utils.PushDownAnim
import com.gigforce.common_ui.utils.RatioLayoutManager
import com.gigforce.common_ui.utils.getScreenWidth
import com.gigforce.core.datamodels.client_activation.Dependency
import kotlinx.android.synthetic.main.layout_questionnaire_fragment.*
import java.util.*


class QuestionnaireFragment : BaseFragment(), AdapterQuestionnaire.AdapterQuestionnaireCallbacks,
    RejectionDialog.RejectionDialogCallbacks {
    private var parentPosition: Int = -1
    private var childPosition: Int = -1
    private lateinit var ratioLayoutManager: RatioLayoutManager
    private var FROM_CLIENT_ACTIVATON: Boolean = false
    private lateinit var mJobProfileId: String
    private lateinit var mType: String
    private lateinit var mTitle: String


    private lateinit var list: ArrayList<Dependency>

    private lateinit var viewModel: ViewModelQuestionnaire
    private var selectedPosition = 0
    private val adapter: AdapterQuestionnaire by lazy {
        AdapterQuestionnaire()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.layout_questionnaire_fragment, inflater, container)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)

        viewModel =
            ViewModelProvider(
                this,
                SavedStateViewModelFactory(requireActivity().application, this)
            ).get(ViewModelQuestionnaire::class.java)
        setupRecycler()
        initObservers()
        initClicks()

        //need to resolve this : list not refreshing after state city loaded
        Handler().postDelayed({
            adapter.notifyDataSetChanged()
        }, 1000)
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(StringConstants.JOB_PROFILE_ID.value, mJobProfileId)
        outState.putBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, FROM_CLIENT_ACTIVATON)
        outState.putString(StringConstants.TYPE.value, mType)
        outState.putString(StringConstants.TITLE.value, mTitle)


    }

    override fun onBackPressed(): Boolean {
        if (FROM_CLIENT_ACTIVATON) {
            navFragmentsData?.setData(
                bundleOf(
                    StringConstants.BACK_PRESSED.value to true

                )
            )

            popBackState()
            return true
        }
        return super.onBackPressed()

    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)


        }

        arguments?.let {
            mJobProfileId = it.getString(StringConstants.JOB_PROFILE_ID.value) ?: return@let
            mType = it.getString(StringConstants.TYPE.value) ?: return@let
            mTitle = it.getString(StringConstants.TITLE.value) ?: return@let
            FROM_CLIENT_ACTIVATON =
                it.getBoolean(StringConstants.FROM_CLIENT_ACTIVATON.value, false)


        }
    }


    private fun initClicks() {
        val smoothScroller: SmoothScroller = object : LinearSmoothScroller(context) {


            override fun getHorizontalSnapPreference(): Int {
                return SNAP_TO_START
            }
        }
        iv_back_application_client_activation.setOnClickListener {
            onBackPressed()
        }
        PushDownAnim.setPushDownAnimTo(tv_action_questionnaire)
            .setOnClickListener(View.OnClickListener {

                if (selectedPosition == adapter.items.size - 1 && adapter.items[selectedPosition].selectedAnswer != -1) {
                    val items = adapter.items.filter { questions ->
                        questions.type == "mcq" &&
                                (questions.options.size > questions.selectedAnswer && !questions.options[questions.selectedAnswer].isAnswer) ||
                                questions.type == "date" && checkForDateRange(questions)
                    }
                    if (items.isEmpty()) {
                        pb_questionnaire.visible()
                        viewModel.addQuestionnaire(
                            mJobProfileId,
                            mTitle,
                            mType,
                            adapter.items
                        )
                    } else {
                        val rejectionDialog = RejectionDialog()
                        rejectionDialog.setCallbacks(this)
                        rejectionDialog
                        rejectionDialog.arguments = bundleOf(
                            StringConstants.REJECTION_TYPE.value to RejectionDialog.REJECTION_QUESTIONNAIRE,
                            StringConstants.TITLE.value to viewModel.observableQuestionnaireResponse.value?.rejectionTitle,
                            StringConstants.REJECTION_ILLUSTRATION.value to viewModel.observableQuestionnaireResponse.value?.rejectionIllustration,
                            StringConstants.CONTENT.value to items.map { it.rejectionPoint }
                        )
                        rejectionDialog.show(
                            parentFragmentManager,
                            DrivingCertSuccessDialog::class.java.name
                        )

                    }

                    return@OnClickListener
                }
                if (selectedPosition > -1 && adapter.items[selectedPosition].selectedAnswer != -1) {
                    selectedPosition += 1
                    ratioLayoutManager.setScrollEnabled(true)
                    smoothScroller.targetPosition = selectedPosition
                    ratioLayoutManager.startSmoothScroll(smoothScroller)
                    adapter.notifyItemChanged(selectedPosition)
                    rv_questionnaire.postDelayed({
                        ratioLayoutManager.setScrollEnabled(false)

                    }, 500)


                } else {
                    showToast(getString(R.string.answer_the_ques))

                }


            })


    }

    private fun checkForDateRange(questions: Questions): Boolean {
        val selectedDate: Date = questions.selectedDate ?: Date()
        val quesObj = questions.validation
        if (quesObj?.validationRequire == true) {
            if (quesObj.outRangeRequire) {
                val split = quesObj.outRange?.beforeDate?.split(":")
                val yearToMinus = split?.get(0)?.toInt() ?: 0
                val monthToMinus = split?.get(1)?.toInt() ?: 0
                val daysToMinus = split?.get(2)?.toInt() ?: 0
                val currentDate = Calendar.getInstance()
                currentDate.add(Calendar.YEAR, -yearToMinus)
                currentDate.add(Calendar.MONTH, -monthToMinus)
                currentDate.add(Calendar.DAY_OF_YEAR, -daysToMinus)
                return (currentDate.time.before(selectedDate))
            }
        }
        return false
    }

    private fun initObservers() {
        viewModel.observableStates.observe(viewLifecycleOwner, Observer {
            if (it.isNullOrEmpty()) return@Observer
            val stateHeader = getString(R.string.current_state)
            if (it[0] != States(name = stateHeader)) {
                it.add(0, States(name = stateHeader))
            }
            adapter.setStates(it, parentPosition)
        })

        viewModel.observableCities.observe(viewLifecycleOwner, Observer {

            val cityHeader = getString(R.string.current_city)
            if (it.isNullOrEmpty()) {
                adapter.setCities(mutableListOf(Cities(name = cityHeader)), parentPosition)
            } else if (it[0] != Cities(name = cityHeader)) {
                it.add(0, Cities(name = cityHeader))
                adapter.setCities(it, parentPosition)

            }

        })
        viewModel.observableAllCities.observe(viewLifecycleOwner, Observer {
            adapter.setAllCities(it, parentPosition, childPosition)
        })
        viewModel.observableError.observe(viewLifecycleOwner, Observer {
            pb_questionnaire.gone()
            showToast(it ?: "")
        })
        viewModel.observableAddApplicationSuccess.observe(viewLifecycleOwner, Observer {
            pb_questionnaire.gone()
            if (it) {
                popBackState()
            }
        })
        viewModel.observableQuestionnaireResponse.observe(viewLifecycleOwner, Observer {
            pb_questionnaire.gone()
            tv_tile_questionnaire.text = it.title
            setupTabs(it.questions.size)
            adapter.addData(it.questions)
        })
        if (!viewModel.initialized) {
            viewModel.getQuestionnaire(mJobProfileId)
        }


    }

    private fun setupRecycler() {
        (rv_questionnaire.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false

        rv_questionnaire.adapter = adapter
        adapter.setCallbacks(this)
        val ratioToCover = 0.85f
        ratioLayoutManager = RatioLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false,
            ratioToCover
        )
        ratioLayoutManager.setScrollEnabled(false)
        rv_questionnaire.layoutManager = ratioLayoutManager
        val snapHelper = PagerSnapHelper()
        snapHelper.attachToRecyclerView(rv_questionnaire)
        rv_questionnaire.addItemDecoration(
            RVPagerSnapFancyDecorator(
                requireContext(),
                (getScreenWidth(requireActivity()).width * ratioToCover).toInt(),
                0.015f
            )
        )
        rv_questionnaire.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                tb_layout_questionnaire.getTabAt(ratioLayoutManager.findFirstCompletelyVisibleItemPosition())
                    ?.select()
            }
        })

    }

    private fun setupTabs(size: Int) {
        tb_layout_questionnaire.removeAllTabs()
        for (i in 0 until size) {
            val newTab = tb_layout_questionnaire.newTab()
            tb_layout_questionnaire.addTab(newTab)
        }
        val tabStrip = tb_layout_questionnaire.getChildAt(0) as LinearLayout
        for (i in 0 until tabStrip.childCount) {
            tabStrip.getChildAt(i).setOnTouchListener { _, _ -> true }
        }

    }

    override fun getStates(childPosition: Int, parentPosition: Int) {
        this.childPosition = childPosition
        this.parentPosition = parentPosition
        viewModel.getState()

    }

    override fun getCities(state: States, parentPosition: Int) {
        this.parentPosition = parentPosition
        viewModel.getCities(state)
    }

    override fun getAllCities(adapterPosition: Int, childPosition: Int) {
        this.parentPosition = adapterPosition
        this.childPosition = childPosition
        viewModel.getAllCities()
    }

    override fun refresh() {
//        adapter.notifyDataSetChanged()
    }

    override fun onClickRefer() {
        navigate(R.id.referrals_fragment)
    }

    override fun onClickTakMeHome() {
        findNavController().popBackStack(R.id.landinghomefragment, true)
    }
}