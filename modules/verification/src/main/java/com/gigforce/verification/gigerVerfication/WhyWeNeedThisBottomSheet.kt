package com.gigforce.verification.gigerVerfication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import com.gigforce.verification.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.android.synthetic.main.fragment_we_why_need_this.*


class WhyWeNeedThisBottomSheet : BottomSheetDialogFragment() {

    private lateinit var title: String
    private lateinit var content: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = inflater.inflate(R.layout.fragment_we_why_need_this, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            title = it.getString(INTENT_EXTRA_TITLE) ?: return@let
            content = it.getString(INTENT_EXTRA_CONTENT) ?: return@let
        }

        savedInstanceState?.let {
            title = it.getString(INTENT_EXTRA_TITLE) ?: return@let
            content = it.getString(INTENT_EXTRA_CONTENT) ?: return@let
        }

        initView()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(INTENT_EXTRA_TITLE, title)
        outState.putString(INTENT_EXTRA_CONTENT, content)
    }

    private fun initView() {
        titleTV.text = title
        contentTV.text = content

        gotItBtn.setOnClickListener { dismiss() }
        close.setOnClickListener { dismiss() }
    }

    companion object {
        const val TAG = "SelectImageSourceBottomSheet"
        const val INTENT_EXTRA_TITLE = "title"
        const val INTENT_EXTRA_CONTENT = "content"

        fun launch(
            childFragmentManager: FragmentManager,
            title: String,
            content: String
        ) {
            val bundle = bundleOf(
                INTENT_EXTRA_TITLE to title,
                INTENT_EXTRA_CONTENT to content
            )

            val fragment = WhyWeNeedThisBottomSheet()
            fragment.arguments = bundle
            fragment.show(childFragmentManager, TAG)
        }
    }
}