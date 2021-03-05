package com.gigforce.app.modules.client_activation

import android.app.Dialog
import android.graphics.Paint
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.gigforce.app.R
import com.gigforce.app.core.gone
import com.gigforce.app.core.visible
import com.gigforce.common_ui.StringConstants
import com.gigforce.common_ui.decors.ItemOffsetDecoration
import com.gigforce.common_ui.utils.getCircularProgressDrawable
import com.gigforce.common_ui.utils.getScreenWidth
import kotlinx.android.synthetic.main.layout_rejection_dialog.*

open class RejectionDialog : DialogFragment() {

    companion object {
        const val REJECTION_NORMAL = 0
        const val REJECTION_QUESTIONNAIRE = 1
    }

    open var CONTENT: ArrayList<String>? = null
    open var TYPE: Int = REJECTION_NORMAL
    open var TITLE: String? = null
    open var ILLUSTRATION: String? = null
    private lateinit var callbacks: RejectionDialogCallbacks
    open val adapter: AdapterRejectedAnswers by lazy {
        AdapterRejectedAnswers()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.layout_rejection_dialog, container, false)
    }

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            dialog.window?.setLayout(
                getScreenWidth(requireActivity()).width - resources.getDimensionPixelSize(
                    R.dimen.size_48
                ), ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getDataFromIntents(savedInstanceState)
        initClicks()
        initView()
        initUiAsPerState()
    }

    open fun initUiAsPerState() {
        if (TYPE == REJECTION_QUESTIONNAIRE) {
            stateQuestionnaireRejection()
        }


    }

    private fun stateQuestionnaireRejection() {
        setupRecycler()
        tv_sub_one_rejection_dialog.gone()
        tv_sub_two_rejection_dialog.gone()
        tv_content_title_rejection_dialog.text = Html.fromHtml(TITLE)
        Glide.with(this).load(ILLUSTRATION).placeholder(
            getCircularProgressDrawable(
                requireContext()
            )
        ).into(iv_rejection_illustration)
        rv_wrong_questions_rejection_dialog.visible()
    }

    open fun setupRecycler() {
        rv_wrong_questions_rejection_dialog.adapter = adapter
        rv_wrong_questions_rejection_dialog.layoutManager = LinearLayoutManager(requireContext())
        rv_wrong_questions_rejection_dialog.addItemDecoration(
            ItemOffsetDecoration(
                resources.getDimensionPixelSize(
                    R.dimen.size_16
                )
            )
        )
        adapter.addData(CONTENT ?: listOf())


    }

    open fun getDataFromIntents(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            TYPE = it.getInt(StringConstants.REJECTION_TYPE.value)
            CONTENT = it.getStringArrayList(StringConstants.CONTENT.value)
                ?: arrayListOf()
            TITLE = it.getString(StringConstants.TITLE.value) ?: ""
            ILLUSTRATION =
                it.getString(StringConstants.ILLUSTRATION.value) ?: ""

        }

        arguments?.let {
            TYPE = it.getInt(StringConstants.REJECTION_TYPE.value)
            CONTENT = it.getStringArrayList(StringConstants.CONTENT.value)
                ?: arrayListOf()
            TITLE = it.getString(StringConstants.TITLE.value) ?: ""
            ILLUSTRATION =

                it.getString(StringConstants.ILLUSTRATION.value) ?: ""

        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putStringArrayList(StringConstants.CONTENT.value, CONTENT)
        outState.putInt(StringConstants.FROM_CLIENT_ACTIVATON.value, TYPE)
        outState.putString(StringConstants.TITLE.value, TITLE)
        outState.putString(StringConstants.ILLUSTRATION.value, ILLUSTRATION)


    }


    private fun initView() {
        tv_take_me_home_rejection_dialog.paintFlags =
            tv_take_me_home_rejection_dialog.paintFlags or Paint.UNDERLINE_TEXT_FLAG

    }

    fun initClicks() {
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