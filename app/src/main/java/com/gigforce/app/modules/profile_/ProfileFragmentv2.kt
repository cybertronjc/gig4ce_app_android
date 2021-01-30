package com.gigforce.app.modules.profile_

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.gigforce.app.R
import com.gigforce.app.core.base.BaseFragment
import com.gigforce.app.utils.PushDownAnim
import kotlinx.android.synthetic.main.add_content_profile_v2.view.*
import kotlinx.android.synthetic.main.fragment_profile_v2.*

class ProfileFragmentv2 : BaseFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflateView(R.layout.fragment_profile_v2, inflater, container)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews() {
        initLanguageCard()

    }

    private fun initLanguageCard() {
        card_language_profile_v2.iv_content_top_icon_profile_v2.setImageResource(R.drawable.ic_language_title)
        card_language_profile_v2.iv_content_illustration_profile_v2.setImageResource(R.drawable.ic_add_language_illustration)
        card_language_profile_v2.tv_content_heading_profile_v2.text = getString(R.string.add_lang)
        card_language_profile_v2.tv_content_top_profile_v2.text = getString(R.string.education)
        card_language_profile_v2.tv_content_heading_profile_v2.text =
            getString(R.string.add_lang_known)
        card_language_profile_v2.tv_add_now_profile_v2.text = getString(R.string.add_now)
        card_language_profile_v2.tv_content_text_profile_v2.text =
            "Lorem Ipsum is simply dummy .Lorem Ipsum is simply dummy. Lorem Ipsum"
        PushDownAnim.setPushDownAnimTo(card_language_profile_v2.tv_add_now_profile_v2)
            .setOnClickListener(
                View.OnClickListener {
                    navigate(R.id.fragment_add_lang_profile_v2)
                })

    }
}