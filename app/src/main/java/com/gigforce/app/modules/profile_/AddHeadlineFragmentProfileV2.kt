package com.gigforce.app.modules.profile_

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import kotlinx.android.synthetic.main.layout_add_headline_profile_v2.*

class AddHeadlineFragmentProfileV2 : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.layout_add_headline_profile_v2, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
        initClicks()
    }

    private fun initClicks() {
        iv_back_application_add_headline_v2.setOnClickListener { activity?.onBackPressed() }

    }

    private fun initViews() {
        var text = ""
        et_add_bio_add_headline.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                text = s.toString()


            }

            override fun onTextChanged(text: CharSequence, start: Int, before: Int, count: Int) {
                tv_save_add_headline.visibility = if (text.isNotEmpty()) View.VISIBLE else View.GONE
                tv_char_counter_add_headline.text = "${text.length}/60"


            }

            override fun afterTextChanged(s: Editable) {
                if (et_add_bio_add_headline.lineCount > 2) {
                    et_add_bio_add_headline.setText(text);
                    val length = tv_save_add_headline.text.toString().length
                    et_add_bio_add_headline.setSelection(length)
                }
            }
        })
    }
}