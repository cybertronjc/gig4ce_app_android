package com.gigforce.app.modules.client_activation

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.app.utils.ItemOffsetDecoration
import com.gigforce.app.utils.StringConstants
import com.gigforce.app.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_rejection_dialog.*

class RejectionDialog : DialogFragment() {

    companion object {
        const val REJECTION_NORMAL = 0
        const val REJECTION_QUESTIONNAIRE = 1
    }

    private lateinit var WRONGQUESTIONS: ArrayList<String>
    private var REJECTIONTYPE: Int = REJECTION_NORMAL
    private lateinit var callbacks: RejectionDialogCallbacks
    private val adapter: AdapterRejectedAnswers by lazy {
        AdapterRejectedAnswers()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.layout_rejection_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(getScreenWidth(requireActivity()).width - resources.getDimensionPixelSize(R.dimen.size_48), ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initClicks()
        initView()
        initUiAsPerState()
    }

    private fun initUiAsPerState() {
        if (REJECTIONTYPE == REJECTION_QUESTIONNAIRE) {
            stateQuestionnaireRejection()
        }


    }

    private fun stateQuestionnaireRejection() {
        setupRecyclerWrongAnswers()
        tv_sub_one_rejection_dialog.gone()
        tv_sub_two_rejection_dialog.gone()
        tv_content_title_rejection_dialog.text="Due to answers of the following questions your application is not approved"
        rv_wrong_questions_rejection_dialog.visible()
    }

    private fun setupRecyclerWrongAnswers() {
        rv_wrong_questions_rejection_dialog.adapter = adapter
        rv_wrong_questions_rejection_dialog.layoutManager = LinearLayoutManager(requireContext())
        rv_wrong_questions_rejection_dialog.addItemDecoration(ItemOffsetDecoration(resources.getDimensionPixelSize(R.dimen.size_16)))
        adapter.addData(WRONGQUESTIONS)


    }

    private fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            REJECTIONTYPE = it.getInt(StringConstants.REJECTION_TYPE.value)
            WRONGQUESTIONS = it.getStringArrayList(StringConstants.WRONG_ANSWERS.value)
                    ?: arrayListOf()
        }

        arguments?.let {
            REJECTIONTYPE = it.getInt(StringConstants.REJECTION_TYPE.value)
            WRONGQUESTIONS = it.getStringArrayList(StringConstants.WRONG_ANSWERS.value)
                    ?: arrayListOf()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(StringConstants.WRONG_ANSWERS.value, WRONGQUESTIONS)
        outState.putInt(StringConstants.FROM_CLIENT_ACTIVATON.value, REJECTIONTYPE)


    }


    private fun initView() {
        tv_take_me_home_rejection_dialog.paintFlags = tv_take_me_home_rejection_dialog.paintFlags or Paint.UNDERLINE_TEXT_FLAG;

    }

    private fun initClicks() {
        tv_refer_rejection_dialog.setOnClickListener {
            dismiss()
            callbacks.onClickRefer()

        }
        tv_take_me_home_rejection_dialog.setOnClickListener {
            dismiss()
            callbacks.onClickTakMeHome()

        }
    }

    fun setCallbacks(callbacks: RejectionDialogCallbacks) {
        this.callbacks = callbacks
    }

    interface RejectionDialogCallbacks {
        fun onClickRefer()
        fun onClickTakMeHome()
    }


}